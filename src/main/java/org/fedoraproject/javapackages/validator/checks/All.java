package org.fedoraproject.javapackages.validator.checks;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.fedoraproject.javapackages.validator.Check;

public class All {
    private static final String PACKAGE_NAME = All.class.getPackageName();
    private static final String PACKAGE_PATH = PACKAGE_NAME.replace('.', '/');

    private static Map<String, Class<?>> findAllChecks() throws Exception {
        var result = new TreeMap<String, Class<?>>();
        String content;
        new BytecodeVersionCheck();
        try (var is = ClassLoader.getSystemResourceAsStream(PACKAGE_PATH)) {
            content = new String(is.readAllBytes(), StandardCharsets.US_ASCII);
        }
        for (String line : content.split("\\R")) {
            if (line.endsWith(".class")) {
                line = line.substring(0, line.length() - 6);
                result.put(line, Class.forName(PACKAGE_NAME + "." + line));
            }
        }
        return result;
    }

    private static Map<String, Class<?>> findAllChecksClasspath() throws Exception {
        var result = new TreeMap<String, Class<?>>();

        for (String p : System.getProperty("java.class.path").split(":")) {
            if (p.endsWith(".jar")) {
                try (var is = new JarArchiveInputStream(new FileInputStream(Paths.get(p).toFile()))) {
                    for (JarArchiveEntry jarEntry; ((jarEntry = is.getNextJarEntry()) != null);) {
                        String className = jarEntry.getName();
                        if (!jarEntry.isDirectory() && className.endsWith(".class") &&
                                className.startsWith(PACKAGE_PATH) && !className.contains("$")) {
                            className = className.substring(0, className.length() - 6);
                            className = className.replace('/', '.');
                            result.put(className.substring(className.lastIndexOf('.') + 1), Class.forName(className));
                        }
                    }
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        var checks = findAllChecksClasspath();
        checks.remove(All.class.getSimpleName());
        for (var entry : checks.entrySet()) {
            if (entry.getKey().contains("$")) {
                continue;
            }
            System.out.println(entry.getKey());
            Check<?> check = Check.class.cast(entry.getValue().getConstructor().newInstance());
            check.executeCheck(Arrays.copyOf(args, args.length));
        }
    }
}
