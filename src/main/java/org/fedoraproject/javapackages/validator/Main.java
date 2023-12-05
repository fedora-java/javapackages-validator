package org.fedoraproject.javapackages.validator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.ToolProvider;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.compress.utils.Iterators;
import org.apache.commons.io.FileUtils;
import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.Decoration;
import org.fedoraproject.javapackages.validator.spi.LogEntry;
import org.fedoraproject.javapackages.validator.spi.LogEvent;
import org.fedoraproject.javapackages.validator.spi.ResultBuilder;
import org.fedoraproject.javapackages.validator.spi.TestResult;
import org.fedoraproject.javapackages.validator.spi.Validator;
import org.fedoraproject.javapackages.validator.spi.ValidatorFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.kojan.javadeptools.rpm.RpmPackage;

public class Main {
    static TextDecorator DECORATOR = TextDecorator.NO_DECORATOR;
    static PrintStream debugOutputStream = new PrintStream(OutputStream.nullOutputStream(), false, StandardCharsets.UTF_8);

    protected Parameters parameters;
    protected Logger logger;
    protected Map<String, ResultBuilder> reports = new TreeMap<>();

    public static TextDecorator getDecorator() {
        return DECORATOR;
    }

    @SuppressFBWarnings({"MS_EXPOSE_REP"})
    public static PrintStream getDebugOutputStream() {
        return debugOutputStream;
    }

    static record Flag(String... options) {
        static final Flag SOURCE_PATH = new Flag("-sp", "--source-path");
        static final Flag OUTPUT_DIRECTORY = new Flag("-d");
        static final Flag CLASS_PATH = new Flag("-cp", "--class-path");

        static final Flag FILE = new Flag("-f", "--file");
        // static final Flag URL = new Flag("-u", "--url");

        static final Flag HELP = new Flag("-h", "--help");
        static final Flag COLOR = new Flag("-r", "--color");
        static final Flag DEBUG = new Flag("-x", "--debug");

        public boolean equals(String arg) {
            return Stream.of(options()).anyMatch(arg::equals);
        }

        @Override
        public String toString() {
            return Stream.of(options()).collect(Collectors.joining(", "));
        }

        static final Flag[] ALL_FLAGS = new Flag[] {
            SOURCE_PATH, OUTPUT_DIRECTORY, CLASS_PATH, FILE, /*URL,*/ HELP, COLOR, DEBUG,
        };
    }

    static void printHelp() {
        System.out.println("Usage: Main [optional flags] <validator factory name | test name>... [test arguments] <-f | -u RPM files or directories to test>...");
        System.out.println("    " + Flag.HELP + " - Print help message");
        System.out.println();
        System.out.println("Options for specifying validators:");
        System.out.println("    " + Flag.SOURCE_PATH + " - File path of a source file");
        System.out.println("    " + Flag.OUTPUT_DIRECTORY + " - Output directory for the sources");
        System.out.println("    " + Flag.CLASS_PATH + " - Additional class path entry");
        System.out.println();
        System.out.println("Test names can be immediately followed by space-separated square parentheses");
        System.out.println("the contents of which will be passed as arguments to the test.");
        System.out.println();
        System.out.println("Options for specifying tested RPM files, can be specified multiple times:");
        System.out.println("    " + Flag.FILE + " - File path of an .rpm file");
        // System.out.println("    " + Flag.URL + " - URL of an .rpm file");
        System.out.println();
        System.out.println("Optional flags:");
        System.out.println("    " + Flag.DEBUG + " - Display debugging output");
        System.out.println("    " + Flag.COLOR + " - Display colored output");
    }

    private static Optional<FileTime> getRecursiveFileTime(Path path, BiPredicate<Path, BasicFileAttributes> filter) {
    	try (var stream = Files.find(path, Integer.MAX_VALUE, filter, FileVisitOption.FOLLOW_LINKS)) {
    	    return stream.map((p) -> {
    	        try {
    	            return Files.getLastModifiedTime(p);
	            } catch (IOException ex) {
	                throw new RuntimeException(ex);
                }
	        }).max(Comparator.naturalOrder());
	    } catch (IOException ex) {
	        throw new RuntimeException(ex);
    	}
    }

    protected static Object decorate(Decorated decorated) {
        return new Object() {
            @Override
            public String toString() {
                return Main.getDecorator().decorate(decorated);
            }
        };
    }

    public static void compileFiles(Path sourcePath, Path outputDirectory, List<Path> classPaths,
            Iterable<String> compilerOptions, Logger logger) throws IOException {
        var sourceMtime = getRecursiveFileTime(sourcePath, (p, a) -> true).get();
        for (var classPath : classPaths) {
            var classMtime = getRecursiveFileTime(classPath, (p, a) -> true).get();
            if (sourceMtime.compareTo(classMtime) < 0) {
                sourceMtime = classMtime;
            }
        }

        if (Files.isSymbolicLink(outputDirectory)) {
            outputDirectory = Files.readSymbolicLink(outputDirectory);
        } else {
            Files.createDirectories(outputDirectory);
        }

        var targetMtime = getRecursiveFileTime(outputDirectory, (p, a) -> !a.isDirectory());
        var targetDMtime = getRecursiveFileTime(outputDirectory, (p, a) -> a.isDirectory()).get();

        boolean recompile = false;

        if (targetMtime.isEmpty()) {
            recompile = true;
            logger.debug("No class files are present on the specified output directory");
        } else if (sourceMtime.compareTo(targetMtime.get()) > 0) {
            recompile = true;
            logger.debug("Source path is more up-to-date than the specified output directory");
        } else if (targetDMtime.compareTo(targetMtime.get()) > 0) {
            recompile = true;
            logger.debug("Output directory has possibly been modified");
        }

        if (recompile) {
            FileUtils.cleanDirectory(outputDirectory.toFile());
            var javac = ToolProvider.getSystemJavaCompiler();
            var fileManager = javac.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8);

            var sourceFiles = Files.find(sourcePath, Integer.MAX_VALUE, (path, attributes) ->
                !attributes.isDirectory() && path.toString().endsWith(".java"), FileVisitOption.FOLLOW_LINKS
            ).toList();
            var compilationUnits = fileManager.getJavaFileObjectsFromPaths(sourceFiles);

            logger.debug("Compiling source files: {0}", Decorated.plain(sourceFiles));

            if (!classPaths.isEmpty()) {
                compilerOptions = IterableUtils.chainedIterable(compilerOptions, List.of("-cp",
                        classPaths.stream().map(Path::toString).collect(Collectors.joining(":"))));
            }

            try {
                if (!javac.getTask(null, fileManager, null, compilerOptions, null, compilationUnits).call()) {
                    throw new RuntimeException("Failed to compile sources");
                }
            } finally {
                fileManager.close();
            }
        } else {
            logger.debug("Not recompiling source files");
        }
    }

    static int tryReadArgs(Map<String, Optional<List<String>>> result, String[] args, int pos) {
        var origPos = pos;
        var vArgs = Optional.<List<String>>empty();

        if (pos + 1 < args.length && args[pos + 1].equals("[")) {
            pos += 2;
            var begin = pos;

            while (!args[pos].equals("]")) {
                ++pos;

                if (pos == args.length) {
                    throw new RuntimeException("Missing ending ] parenthesis");
                }
            }

            var argsList = new ArrayList<String>(pos - begin);
            for (int i = begin; i != pos; ++i) {
                argsList.add(args[i]);
            }
            vArgs = Optional.of(argsList);
        }

        result.put(args[origPos], vArgs);
        return pos;
    }

    private Path resolveRelativePathCommon(String path) {
        var result = Paths.get(path);

        if (!result.isAbsolute()) {
            result = resolveRelativePath(result);
        }

        return result;
    }

    protected Path resolveRelativePath(Path path) {
        return path;
    }

    protected static class Parameters {
        Path sourcePath = null;
        Path outputDir = null;
        List<Path> classPaths = new ArrayList<>(0);
        List<Path> argPaths = new ArrayList<>(0);
        Set<String> factories = new TreeSet<String>();
        Map<String, Optional<List<String>>> validatorArgs = new LinkedHashMap<>();
    }

    @SuppressFBWarnings({"DM_EXIT", "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
    void parseArguments(String[] args) throws Exception {
        parameters = new Parameters();

        if (args.length == 0) {
            System.out.println("error: no arguments provided");
            printHelp();
            System.exit(1);
        } else if (Flag.HELP.equals(args[0])) {
            printHelp();
            System.exit(0);
        }

        Flag lastFlag = null;
        for (int i = 0; i != args.length; ++i) {
            if (args[i].startsWith("-")) {
                lastFlag = null;

                for (Flag flag : Flag.ALL_FLAGS) {
                    if (flag.equals(args[i])) {
                        lastFlag = flag;
                        ++i;
                        break;
                    }
                }

                if (lastFlag == null) {
                    throw new RuntimeException("Unrecognized option: " + args[i]);
                }
            } else {
                lastFlag = null;
            }

            if (lastFlag == null) {
                if (args[i].startsWith("/")) {
                    i = tryReadArgs(parameters.validatorArgs, args, i);
                } else {
                    parameters.factories.add(args[i]);
                }
            } else if (lastFlag == Flag.COLOR) {
                DECORATOR = AnsiDecorator.INSTANCE;
                --i;
            } else if (lastFlag == Flag.DEBUG) {
                debugOutputStream = System.err;
                --i;
            } else if (lastFlag == Flag.SOURCE_PATH) {
                parameters.sourcePath = resolveRelativePathCommon(args[i]);
            } else if (lastFlag == Flag.OUTPUT_DIRECTORY) {
                parameters.outputDir = resolveRelativePathCommon(args[i]);
            } else if (lastFlag == Flag.CLASS_PATH) {
                parameters.classPaths.add(resolveRelativePathCommon(args[i]));
            } else if (lastFlag == Flag.FILE) {
                parameters.argPaths.add(Paths.get(args[i]));
            }
        }

        var validatorPath = Paths.get(MainTmt.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        parameters.classPaths.add(validatorPath);
        var parent = validatorPath.getParent();
        if (parent != null) {
            Files.find(parent.resolve("dependency"), 1, (path, attributes) -> {
                var filename = path.getFileName().toString();
                return attributes.isRegularFile() && filename.startsWith("java-deptools-native") && filename.endsWith(".jar");
            }).forEach(parameters.classPaths::add);
        }

        logger = new Logger();

        logger.debug("Source path: {0}", Decorated.plain(parameters.sourcePath));
        logger.debug("Output directory: {0}", Decorated.plain(parameters.outputDir));
        logger.debug("Class path: {0}", Decorated.plain(parameters.classPaths));
        logger.debug("Path arguments: {0}", Decorated.plain(parameters.argPaths));
        // logger.debug("URL arguments: {0}", Decorated.list(parameters.argUrls));
    }

    private Map<String, Validator> discover() throws Exception {
        if (parameters.sourcePath != null && parameters.outputDir == null) {
            throw new RuntimeException("If source path is specified then the output directory needs to be specified too");
        }

        if (parameters.outputDir != null) {
            var compilerArgs = new ArrayList<String>();
            compilerArgs.add("--enable-preview");
            compilerArgs.add("--release");
            compilerArgs.add("21");
            compilerArgs.add("-d");
            compilerArgs.add(parameters.outputDir.toString());

            compileFiles(parameters.sourcePath, parameters.outputDir, parameters.classPaths, compilerArgs, logger);

            var serviceContent = new ByteArrayOutputStream(0);
            for (var serviceFile : Files.find(parameters.sourcePath, Integer.MAX_VALUE,
                    (p, a) -> !a.isDirectory() && p.getFileName().equals(Paths.get(ValidatorFactory.class.getCanonicalName()))).toList()) {
                try (var is = Files.newInputStream(serviceFile)) {
                    is.transferTo(serviceContent);
                }
            }
            if (serviceContent.size() != 0) {
                var serviceOutFile = Files.createDirectories(parameters.outputDir
                        .resolve("META-INF").resolve("services")).resolve(ValidatorFactory.class.getCanonicalName());
                try (var os = Files.newOutputStream(serviceOutFile)) {
                    new ByteArrayInputStream(serviceContent.toByteArray()).transferTo(os);
                }
            }
        }

        var classPaths = new ArrayList<URL>();
        if (parameters.outputDir != null) {
            classPaths.add(parameters.outputDir.toUri().toURL());
        }
        for (var classPath : parameters.classPaths) {
            classPaths.add(classPath.toUri().toURL());
        }
        var validators = new ArrayList<Validator>();

        logger.debug("Factory arguments: {0}", Decorated.plain(parameters.factories.stream().toList()));

        var oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Do not close the cloassloaders, they are used later
            var classLoader = new URLClassLoader(classPaths.toArray(URL[]::new));
            Thread.currentThread().setContextClassLoader(classLoader);
            ServiceLoader.<ValidatorFactory>load(ValidatorFactory.class, classLoader).stream().forEach(provider -> {
                var factory = provider.get();
                if (parameters.factories.isEmpty() || parameters.factories.contains(factory.getClass().getName())) {
                    validators.addAll(factory.getValidators());
                } else {
                    logger.debug("Ignoring factory {0} as it is not listed as an argument", Decorated.struct(factory.getClass().getName()));
                }
            });
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        var validatorTests = new TreeMap<String, Validator>();

        for (var validator : validators) {
            var testName = validator.getTestName();

            logger.debug("Test {0} is implemented by {1}",
                    Decorated.actual(testName),
                    Decorated.struct(validator.getClass().getName()));

            {
                Function<Validator, LogEntry> duplicate = v -> {
                    return LogEntry.error("Test is implemented by multiple validators: {0}",
                            Decorated.struct(v.getClass().getName()));
                };

                var present = reports.get(testName);
                if (present != null) {
                    present.addLog(duplicate.apply(validator));
                } else {
                    var previousValidator = validatorTests.put(testName, validator);
                    if (previousValidator != null) {
                        var result = new ResultBuilder();
                        result.mergeResult(TestResult.error);
                        result.addLog(duplicate.apply(previousValidator));
                        result.addLog(duplicate.apply(validatorTests.remove(testName)));
                        reports.put(testName, result);
                    }
                }
            }
        }

        for (var testName : reports.keySet()) {
            logger.debug("Test {0} is implemented by multiple validators",
                    Decorated.actual(testName));
        }

        logger.debug("Available tests:{0}", Decorated.plain(validators.stream().map(v ->
            System.lineSeparator() + decorate(Decorated.struct(v.getTestName()))
        ).collect(Collectors.joining())));

        return validatorTests;
    }

    Map<String, Validator> select(Map<String, Validator> validators) throws Exception {
        logger.debug("Main arguments: {0}", Decorated.plain(parameters.validatorArgs.entrySet().stream().map(e -> {
            var result = new StringBuilder();
            result.append(System.lineSeparator());
            result.append(decorate(Decorated.custom(e.getKey(), new Decoration(Decoration.Color.green, Decoration.Modifier.bright))).toString());
            if (e.getValue().isPresent()) {
                var args = e.getValue().get();
                result.append(decorate(Decorated.custom(" [ ", new Decoration(Decoration.Color.blue, Decoration.Modifier.bright))).toString());
                result.append(Stream.of(args).map(a -> decorate(Decorated.custom(a, new Decoration(Decoration.Color.cyan))).toString()).collect(Collectors.joining(" ")));
                result.append(decorate(Decorated.custom(" ]", new Decoration(Decoration.Color.blue, Decoration.Modifier.bright))).toString());
            }
            return result.toString();
        }).collect(Collectors.joining())));

        if (!parameters.validatorArgs.isEmpty()) {
            var it = validators.keySet().iterator();
            while (it.hasNext()) {
                if (!parameters.validatorArgs.containsKey(it.next())) {
                    it.remove();
                }
            }
        }

        var notFoundValidators = new ArrayList<String>(0);
        for (var testNameArg : parameters.validatorArgs.keySet()) {
            if (validators.keySet().stream().filter(testName -> testNameArg.equals(testName)).findFirst().isEmpty()) {
                notFoundValidators.add(testNameArg);
            }
        }

        if (!notFoundValidators.isEmpty()) {
            throw new RuntimeException("The following arguments were not found as available tests: " + notFoundValidators);
        }

        return validators;
    }

    @SuppressFBWarnings({"DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED"})
    List<NamedResult> execute(Collection<Validator> validators) throws Exception {
        var rpms = new ArrayList<RpmPackage>();
        /*
        parameters.argUrls.parallelStream().forEach(path -> {
            RpmPackage rpm;
            try {
                rpm = new RpmPackage(path);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            synchronized (rpms) {
                rpms.add(rpm);
            }
        });
        */
        Iterators.addAll(rpms, new ArgFileIterator(parameters.argPaths));
        var resultList = validators.parallelStream().map(validator -> {
            var oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(validator.getClass().getClassLoader());
                var startTime = LocalDateTime.now();
                var result = validator.validate(rpms, parameters.validatorArgs
                        .getOrDefault(validator.getTestName(), Optional.empty()).orElse(null));
                var endTime = LocalDateTime.now();
                return new NamedResult(result, validator.getTestName(), startTime, endTime);
            } catch (Exception ex) {
                var result = new ResultBuilder();
                result.error(ex);
                return new NamedResult(result.build(), validator.getTestName());
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }).toList();

        for (var validator : validators) {
            if (validator.getClass().getClassLoader() instanceof AutoCloseable ac) {
                ac.close();
            }
        }

        return resultList;
    }

    protected static final String decoratedObjects(LogEntry entry, TextDecorator decorator) {
        return MessageFormat.format(entry.pattern(), Stream.of(entry.objects())
                .map(a -> decorator.decorate(a)).toArray());
    }

    protected static final String decorated(LogEntry entry) {
        return "[" + decorate(entry.kind().getDecorated()) + "] " + decoratedObjects(entry, Main.getDecorator());
    }

    @SuppressFBWarnings({"DM_EXIT"})
    void report(List<NamedResult> results) throws Exception {
        int passMessages = 0;
        for (var result : results) {
            for (var logEntry : result) {
                if (!LogEvent.fail.equals(logEntry.kind()) && !LogEvent.error.equals(logEntry.kind())) {
                    if (LogEvent.debug.equals(logEntry.kind())) {
                        getDebugOutputStream().println(decorated(logEntry));
                    } else {
                        System.out.println(decorated(logEntry));
                    }

                    if (LogEvent.pass.equals(logEntry.kind())) {
                        ++passMessages;
                    }
                }
            }
        }

        int failMessages = 0;
        for (var result : results) {
            for (var logEntry : result) {
                if (LogEvent.fail.equals(logEntry.kind())) {
                    System.out.println(decorated(logEntry));
                    ++failMessages;
                }
            }
        }

        int errorMessages = 0;
        for (var result : results) {
            for (var logEntry : result) {
                if (LogEvent.error.equals(logEntry.kind())) {
                    System.out.println(decorated(logEntry));
                    ++errorMessages;
                }
            }
        }

        var bold_red = new Decoration(Decoration.Color.red, Decoration.Modifier.bold);

        int exitCode = 0;
        if (failMessages == 0 && errorMessages == 0) {
            if (passMessages > 0) {
                System.err.println(MessageFormat.format("Summary: all tests {0}",
                        decorate(Decorated.custom("passed", new Decoration(Decoration.Color.green, Decoration.Modifier.bold)))));
            } else {
                System.err.println("Summary: no output available");
            }
        } else if (errorMessages == 0) {
            System.err.println(MessageFormat.format("Summary: {0} {1}",
                    decorate(Decorated.plain(failMessages)), decorate(Decorated.custom(
                    "failed tests" + (failMessages == 1 ? "" : "s"), bold_red))));
            exitCode = 1;
        } else if (failMessages == 0) {
            System.err.println(MessageFormat.format("Summary: {0} {1} occured",
                    decorate(Decorated.plain(errorMessages)), decorate(Decorated.custom(
                    "error" + (errorMessages == 1 ? "" : "s"), bold_red))));
            exitCode = 2;
        }

        System.exit(exitCode);
    }

    void run(String[] args) throws Exception {
        parseArguments(args);
        var validators = select(discover());

        logger.debug("Selected validators:{0}", Decorated.plain(validators.keySet().stream().map(
                testName -> System.lineSeparator() + decorate(Decorated.struct(testName))
        ).collect(Collectors.joining())));

        report(execute(validators.values()));
    }

    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }
}
