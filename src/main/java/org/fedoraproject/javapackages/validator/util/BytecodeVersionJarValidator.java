package org.fedoraproject.javapackages.validator.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.TestResult;

import io.kojan.javadeptools.rpm.RpmPackage;

public abstract class BytecodeVersionJarValidator extends JarValidator {
    public static record Version(short major, short minor) {
        @Override
        public String toString() {
            return String.valueOf(major) + "." + String.valueOf(minor);
        }
    }

    @Override
    public void acceptJarEntry(RpmPackage rpm, CpioArchiveEntry rpmEntry, byte[] content) throws Exception {
        var jarPath = Path.of(rpmEntry.getName().substring(1));
        var classVersions = new TreeMap<Path, Version>();

        try (var jarStream = new JarInputStream(new ByteArrayInputStream(content))) {
            for (JarEntry jarEntry; (jarEntry = jarStream.getNextJarEntry()) != null;) {
                var classPath = Path.of(jarEntry.getName());

                if (classPath.toString().endsWith(".class")) {
                    var dataInput = new DataInputStream(jarStream);
                    dataInput.readInt(); // magic number
                    var minorVersion = dataInput.readShort();
                    var majorVersion = dataInput.readShort();
                    classVersions.put(classPath, new Version(majorVersion, minorVersion));
                }
            }
        }

        validate(rpm, jarPath, classVersions);

        if (TestResult.pass.equals(getResult())) {
            pass("{0}: {1}: found bytecode versions: {2}",
                    Decorated.rpm(rpm),
                    Decorated.custom(jarPath, DECORATION_JAR),
                    Decorated.actual(classVersions.values().stream().distinct().toList()));
        }
    }

    public void validate(RpmPackage rpm, Path jarPath, Map<Path, Version> classVersions) {
        for (var entry : classVersions.entrySet()) {
            info("{0}: {1}: {2}: bytecode version: {3}",
                    Decorated.rpm(rpm),
                    Decorated.custom(jarPath, DECORATION_JAR),
                    Decorated.struct(entry.getKey()),
                    Decorated.actual(entry.getValue()));
        }
    }
}
