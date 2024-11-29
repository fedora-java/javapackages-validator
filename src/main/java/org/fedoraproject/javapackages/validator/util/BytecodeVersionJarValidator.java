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

/// Abstract class for validating bytecode versions within JAR files inside RPM
/// packages.
///
/// This validator processes JAR files contained in RPM packages and extracts
/// bytecode version information from their compiled `.class` files.
public abstract class BytecodeVersionJarValidator extends JarValidator {

    /// Record representing a Java bytecode version with major and minor numbers.
    ///
    /// @param major The major version number.
    /// @param minor The minor version number.
    public static record Version(short major, short minor) {
        @Override
        public String toString() {
            return major + "." + minor;
        }
    }

    /// Processes an individual JAR entry within an RPM package, extracting bytecode
    /// versions.
    ///
    /// @param rpm      The RPM package containing the JAR file.
    /// @param rpmEntry The archive entry representing the JAR file.
    /// @param content  The byte content of the JAR file.
    /// @throws Exception If an error occurs while reading or processing the JAR
    ///                   file.
    @Override
    public void acceptJarEntry(RpmPackage rpm, CpioArchiveEntry rpmEntry, byte[] content) throws Exception {
        var jarPath = Path.of(rpmEntry.getName().substring(1));
        var classVersions = new TreeMap<Path, Version>();

        try (var jarStream = new JarInputStream(new ByteArrayInputStream(content))) {
            for (JarEntry jarEntry; (jarEntry = jarStream.getNextJarEntry()) != null;) {
                var classPath = Path.of(jarEntry.getName());

                if (classPath.toString().endsWith(".class")) {
                    var dataInput = new DataInputStream(jarStream);
                    dataInput.readInt(); // Skip magic number
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

    /// Validates the extracted bytecode versions of `.class` files within a
    /// JAR.
    ///
    /// @param rpm           The RPM package containing the JAR file.
    /// @param jarPath       The file path of the JAR file inside the RPM.
    /// @param classVersions A map of class file paths to their corresponding
    ///                      bytecode versions.
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
