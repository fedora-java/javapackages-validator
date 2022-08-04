package org.fedoraproject.javapackages.validator.checks;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.utils.Iterators;
import org.fedoraproject.javapackages.validator.Check;
import org.fedoraproject.javapackages.validator.RpmPathInfo;

public class All extends Check<Check.NoConfig> {
    private static final String PACKAGE_NAME = All.class.getPackageName();
    private static final String PACKAGE_PATH = PACKAGE_NAME.replace('.', '/');

    public All() throws Exception {
        super(NoConfig.class);
    }

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

    @Override
    public Collection<String> check(Iterator<? extends RpmPathInfo> testRpms) throws IOException {
        var rpmList = new ArrayList<RpmPathInfo>();
        Iterators.addAll(rpmList, testRpms);

        try {
            var result = new ArrayList<String>();

            var checks = findAllChecksClasspath();
            checks.remove(All.class.getSimpleName());

            for (var entry : checks.entrySet()) {
                if (entry.getKey().contains("$")) {
                    continue;
                }
                Check<?> check = Check.class.cast(entry.getValue().getConstructor().newInstance());
                check.inherit(this);

                if (check.getConfig() == null && !NoConfig.class.equals(check.getConfigClass())) {
                    System.err.println(MessageFormat.format("[INFO] {0}: Configuration class not found, ignoring the test",
                            check.getClass().getSimpleName()));
                } else {
                    result.addAll(check.check(rpmList.iterator()));
                }
            }

            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
