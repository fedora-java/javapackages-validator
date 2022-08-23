package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.ClassUtils;
import org.fedoraproject.javapackages.validator.Main.Flag;
import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;
import org.fedoraproject.javapackages.validator.compiler.InMemoryClassLoader;
import org.fedoraproject.javapackages.validator.compiler.InMemoryFileManager;
import org.fedoraproject.javapackages.validator.compiler.URIJavaFileObject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The base class for checks. Classes that inherit from this must implement
 * either one of the two "check" methods.
 * @param <Config> The config class corresponding to the check. Void may be used
 * if the check is not configurable.
 */
@SuppressFBWarnings({"DMI_HARDCODED_ABSOLUTE_FILENAME"})
public abstract class Check<Config> {
    private Map<Class<?>, Collection<Object>> configurations = null;
    private List<URI> config_uris = new ArrayList<>();
    private Class<Config> configClass;
    private Logger logger = new Logger();

    protected Logger getLogger() {
        return logger;
    }

    protected static String textDecorate(Object object, Decoration... decorations) {
        return Main.getDecorator().decorate(object, decorations);
    }

    protected static String failMessage(String pattern, Object... arguments) {
        String result = "";
        result += "[";
        result += textDecorate("FAIL", Decoration.red, Decoration.bold);
        result += "] ";
        result += MessageFormat.format(pattern, arguments);
        return result;
    }

    public Class<Config> getConfigClass() {
        return configClass;
    }

    protected Check(Class<Config> configClass) {
        this.configClass = configClass;
    }

    private Map<String, ? extends JavaFileObject> compileFiles(Iterable<URI> sourceURIs, Iterable<String> compilerOptions) throws IOException {
        var compilationUnits = new ArrayList<JavaFileObject>();
        for (URI sourceURI : sourceURIs) {
            if (sourceURI.getScheme().equalsIgnoreCase("file")) {
                Files.find(Paths.get(sourceURI), Integer.MAX_VALUE,
                        (path, attributes) -> !attributes.isDirectory() && path.toString().endsWith(".java"))
                        .map((path) -> new URIJavaFileObject(path.toUri(), Kind.SOURCE)).forEach(compilationUnits::add);
            } else {
                compilationUnits.add(new URIJavaFileObject(sourceURI, Kind.SOURCE));
            }
        }

        if (compilationUnits.isEmpty()) {
            logger.debug("No source files found");
            return Collections.emptyMap();
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = new InMemoryFileManager(compiler.getStandardFileManager(null, null, null));

        logger.debug("Compiling source configuration files: {0}", compilationUnits);

        try {
            if (!compiler.getTask(null, fileManager, null, compilerOptions, null, compilationUnits).call()) {
                throw new RuntimeException("Failed to compile configuration sources");
            }
        } finally {
            fileManager.close();
        }

        return fileManager.getOutputs();
    }

    public static interface NoConfig {
    }

    @SuppressFBWarnings({"DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED"})
    protected Collection<Config> getConfigInstances() throws IOException {
        if (configurations == null) {
            configurations = new HashMap<>();

            Map<String, ? extends JavaFileObject> configClasses = compileFiles(config_uris, Arrays.asList());

            logger.debug("Compiled configuration class files: [{0}]", configClasses.keySet().stream()
                    .collect(Collectors.joining(", ")));

            try {
                ClassLoader cl = new InMemoryClassLoader(configClasses);
                for (var className : configClasses.keySet()) {
                    Class<?> cls = cl.loadClass(className);
                    for (var intrfc : ClassUtils.getAllInterfaces(cls)) {
                        Object instance = cls.getConstructor().newInstance();
                        configurations.computeIfAbsent(intrfc, (i) -> new ArrayList<>(1)).add(instance);
                    }
                }
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }

            logger.debug("Configurations: [{0}]", configurations.keySet().stream()
                    .map(Class::getSimpleName).collect(Collectors.joining(", ")));
        }

        return configurations.getOrDefault(configClass, Collections.emptyList()).stream().map(configClass::cast).toList();
    }

    abstract public Collection<String> check(Config config, Collection<RpmPathInfo> testRpms) throws IOException;

    public int executeCheck(String... args) throws IOException {
        List<String> argList = new ArrayList<>();

        for (int i = 0; i != args.length; ++i) {
            if (Flag.CONFIG_URI.equals(args[i])) {
                ++i;
                try {
                    config_uris.add(new URI(args[i]));
                } catch (URISyntaxException ex) {
                    throw new IllegalArgumentException(args[i], ex);
                }
            } else if (Flag.CONFIG_FILE.equals(args[i])) {
                ++i;
                config_uris.add(Paths.get(args[i]).toUri());
            } else if (args[i].startsWith("-")) {
                throw new RuntimeException("Unrecognized option: " + args[i]);
            } else {
                argList.add(args[i]);
            }
        }

        logger.debug("Compile source URIs: {0}", config_uris);
        logger.debug("Arguments: {0}", argList);

        Collection<Config> configInstances;

        if (NoConfig.class.equals(configClass)) {
            Config none = null;
            configInstances = Arrays.asList(none);
        } else {
            configInstances = getConfigInstances();
            if (configInstances.isEmpty()) {
                getLogger().info("{0}: Configuration class not found, ignoring the test", getClass().getSimpleName());
                return 0;
            }
        }

        Main.readTestRpmArgs(argList);

        var messages = new ArrayList<String>();
        for (Config configInstance : configInstances) {
            messages.addAll(check(configInstance, Main.getTestRpms()));
        }

        for (var message : messages) {
            System.out.println(message);
        }

        if (messages.isEmpty()) {
            logger.info("Summary: all checks {0}", textDecorate("passed", Decoration.green, Decoration.bold));
            return 0;
        } else {
            logger.info("Summary: {0} checks {1}", messages.size(), textDecorate("failed", Decoration.red, Decoration.bold));
            return 1;
        }
    }
}
