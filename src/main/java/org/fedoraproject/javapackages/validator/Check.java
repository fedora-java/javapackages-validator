package org.fedoraproject.javapackages.validator;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.ClassUtils;
import org.fedoraproject.javadeptools.rpm.RpmInfo;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The base class for checks. Classes that inherit from this must implement
 * either one of the two "check" methods.
 * @param <Config> The config class corresponding to the check. Void may be used
 * if the check is not configurable.
 */
@SuppressFBWarnings({"DMI_HARDCODED_ABSOLUTE_FILENAME"})
public abstract class Check<Config> {
    private Map<Class<?>, Object> configurations = null;
    private Path config_src_dir = Paths.get("/mnt/config/src");
    private Path config_bin_dir = Paths.get("/mnt/config/bin");
    private Class<Config> declaredConfigClass;
    private Config config;

    protected Class<Config> getDeclaredConfigClass() {
        return declaredConfigClass;
    }

    protected Config getConfig() {
        return config;
    }

    protected Check(Config config) {
        this.config = config;
    }

    private static void compileFiles(Path sourceDir, Iterable<String> compilerOptions) throws IOException {
        List<File> inputFiles = Files.find(sourceDir, Integer.MAX_VALUE,
                (path, attributes) -> attributes.isRegularFile() && path.toString().endsWith(".java"))
                .map(Path::toFile).toList();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        try {
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(inputFiles);
            if (!compiler.getTask(null, fileManager, null, compilerOptions, null, compilationUnits).call()) {
                throw new RuntimeException("Failed to compile configuration sources");
            }
        } finally {
            fileManager.close();
        }
    }

    public static interface NoConfig {
        public static final NoConfig INSTANCE = new NoConfig() {};
    }

    @SuppressFBWarnings({"DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED"})
    protected Config getConfig(Class<Config> configClass) throws IOException {
        if (Files.notExists(config_bin_dir) || Files.find(config_bin_dir, Integer.MAX_VALUE,
                (path, attributes) -> attributes.isRegularFile()).count() == 0) {
            compileFiles(config_src_dir, Arrays.asList("-d", config_bin_dir.toString()));
        }

        if (configurations == null) {
            declaredConfigClass = configClass;
            configurations = new HashMap<>();
            configurations.put(NoConfig.class, NoConfig.INSTANCE);
            var classes = Files.find(config_bin_dir, Integer.MAX_VALUE, (path, attributes) ->
                    attributes.isRegularFile() && path.toString().endsWith(".class"))
                    .map(Path::toString).toArray(String[]::new);

            for (int i = 0; i != classes.length; ++i) {
                classes[i] = classes[i].substring(config_bin_dir.toString().length() + 1);
                classes[i] = classes[i].substring(0, classes[i].length() - 6);
                classes[i] = classes[i].replace('/', '.');
            }

            try (URLClassLoader cl = new URLClassLoader(new URL[] {config_bin_dir.toUri().toURL()})) {
                for (var className : classes) {
                    Class<?> cls = cl.loadClass(className);
                    for (var intrfc : ClassUtils.getAllInterfaces(cls)) {
                        Object instance = cls.getConstructor().newInstance();
                        configurations.computeIfAbsent(intrfc, (i) -> instance);
                    }
                }
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        return configClass.cast(configurations.get(configClass));
    }

    abstract protected Collection<String> check(Iterator<RpmInfo> testRpms) throws IOException;

    private static class ArgFileIterator implements Iterator<RpmInfo> {
        private Iterator<String> argIterator;
        private Iterator<Path> pathIterator = null;

        private ArgFileIterator(List<String> argList) {
            this.argIterator = argList.iterator();
            pathIterator = advance();

            if (pathIterator == null) {
                pathIterator = Arrays.<Path>asList().iterator();
            }
        }

        private Iterator<Path> advance() {
            while (argIterator.hasNext()) {
                Path argPath = Paths.get(argIterator.next()).resolve(".").toAbsolutePath().normalize();

                try {
                    if (Files.isSymbolicLink(argPath)) {
                        argPath = argPath.toRealPath();
                    }

                    if (Files.notExists(argPath)) {
                        throw new RuntimeException("File " + argPath + " does not exist");
                    } else if (Files.isRegularFile(argPath)) {
                        return Arrays.asList(argPath).iterator();
                    } else if (Files.isDirectory(argPath)) {
                        return Files.find(argPath, Integer.MAX_VALUE, (path, attributes) ->
                                attributes.isRegularFile() && path.toString().endsWith(".rpm")).iterator();
                    } else {
                        throw new IllegalStateException("File " + argPath + " of unknown type");
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }

            return null;
        }

        @Override
        public boolean hasNext() {
            if (pathIterator.hasNext()) {
                return true;
            }

            pathIterator = advance();

            if (pathIterator != null) {
                return true;
            }

            return false;
        }

        @Override
        public RpmInfo next() {
            try {
                return new RpmInfo(pathIterator.next());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    protected int executeCheck(Class<Config> configClass, String... args) throws IOException {
        List<String> argList = new ArrayList<>();

        for (int i = 0; i != args.length; ++i) {
            if (args[i].equals("--config-src")) {
                ++i;
                config_src_dir = Paths.get(args[i]);
            } else if (args[i].equals("--config-bin")) {
                ++i;
                config_bin_dir = Paths.get(args[i]);
            } else if (args[i].startsWith("-")) {
                throw new RuntimeException("Unrecognized option: " + args[i]);
            } else {
                argList.add(args[i]);
            }
        }

        config = getConfig(configClass);

        if (config == null && !NoConfig.class.equals(configClass)) {
            System.err.println("[INFO] Configuration class not found, ignoring the test");
            return 0;
        }

        int result = 0;

        for (var message : check(new ArgFileIterator(argList))) {
            result = 1;
            System.out.println(message);
        }

        return result;
    }
}
