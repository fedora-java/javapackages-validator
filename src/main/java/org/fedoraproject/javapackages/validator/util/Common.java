package org.fedoraproject.javapackages.validator.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.io.IOUtils;

import io.kojan.javadeptools.rpm.RpmArchiveInputStream;
import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public class Common {
    public static Path getEntryPath(CpioArchiveEntry entry) {
        return Path.of("/").resolve(Path.of("/").relativize(Path.of("/").resolve(Path.of(entry.getName()))));
    }

    public static String getPackageName(RpmInfo rpm) {
        return rpm.isSourcePackage() ? rpm.getName() : rpm.getSourceName();
    }

    /**
     * @param rpm The RPM package to inspect
     * @return A map of file paths mapped to either the target of the symlink
     * or null, if the file path is not a symlink.
     * @throws IOException
     */
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
