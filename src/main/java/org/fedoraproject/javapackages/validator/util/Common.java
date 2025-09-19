package org.fedoraproject.javapackages.validator.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.io.IOUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.kojan.javadeptools.rpm.RpmArchiveInputStream;
import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

/// Utility class providing common methods for handling RPM packages and archive
/// entries.
public class Common {

    /// Retrieves the file system path for a given CPIO archive entry.
    ///
    /// @param entry The CPIO archive entry to resolve.
    /// @return The resolved file system path of the entry.
    @SuppressFBWarnings(value = {"DMI_HARDCODED_ABSOLUTE_FILENAME"})
    public static Path getEntryPath(CpioArchiveEntry entry) {
        return Path.of("/").resolve(Path.of("/").relativize(Path.of("/").resolve(Path.of(entry.getName()))));
    }

    /// Retrieves the package name of an RPM package.
    ///
    /// @param rpm The RPM package to inspect.
    /// @return The name of the package. If the package is a source package, returns
    ///         its name; otherwise, returns the source package name.
    public static String getPackageName(RpmInfo rpm) {
        return rpm.isSourcePackage() ? rpm.getName() : rpm.getSourceName();
    }

    /// Retrieves a sorted map of file paths from an RPM package mapped to their
    /// symlink targets.
    ///
    /// @param rpm The RPM package to inspect.
    /// @return A sorted map where keys are archive entries representing file paths,
    ///         and values represent the target of the symlink or `null` if the
    ///         file is not a symlink.
    /// @throws IOException If an error occurs while reading the RPM package.
    public static SortedMap<CpioArchiveEntry, Path> rpmFilesAndSymlinks(RpmPackage rpm) throws IOException {
        var result = new TreeMap<CpioArchiveEntry, Path>((lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));

        try (var is = new RpmArchiveInputStream(rpm.getPath())) {
            for (CpioArchiveEntry rpmEntry; (rpmEntry = is.getNextEntry()) != null;) {
                Path target = null;

                if (rpmEntry.isSymbolicLink()) {
                    target = Path.of(IOUtils.toString(is, StandardCharsets.UTF_8));
                }

                result.put(rpmEntry, target);
            }
        }

        return result;
    }
}
