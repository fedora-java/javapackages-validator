package org.fedoraproject.javapackages.validator.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.fedoraproject.javapackages.validator.DefaultValidator;
import org.fedoraproject.javapackages.validator.spi.Decorated;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

/// Abstract class for validating duplicate files across multiple RPM packages.
///
/// This validator checks for duplicate file paths present in multiple RPM
/// packages and determines whether they are allowed based on specific
/// conditions.
public abstract class DuplicateFileValidator extends DefaultValidator {

    /// Validates duplicate files in a collection of RPM packages.
    ///
    /// @param rpms The iterable collection of RPM packages to validate.
    /// @throws Exception If an error occurs while processing the packages.
    @Override
    public void validate(Iterable<RpmPackage> rpms) throws Exception {
        var files = new TreeMap<String, ArrayList<Map.Entry<CpioArchiveEntry, Path>>>();

        for (var rpm : rpms) {
            if (!rpm.getInfo().isSourcePackage()) {
                for (var entry : Common.rpmFilesAndSymlinks(rpm).entrySet()) {
                    files.computeIfAbsent(Common.getEntryPath(entry.getKey()).toString(), _ -> new ArrayList<>())
                        .add(Map.entry(entry.getKey(), rpm.getPath()));
                }
            }
        }

        for (var entry : files.entrySet()) {
            if (entry.getValue().size() > 1) {
                var providers = new ArrayList<RpmInfo>(entry.getValue().size());
                for (var providerEntry : entry.getValue()) {
                    providers.add(new RpmPackage(providerEntry.getValue()).getInfo());
                }
                var okDifferentArchs = new Boolean[] {true};

                // Check if all providers are of different architectures (except noarch)
                providers.sort((lhs, rhs) -> {
                    int cmp = lhs.getArch().compareTo(rhs.getArch());
                    if (cmp == 0 || lhs.getArch().equals("noarch") || rhs.getArch().equals("noarch")) {
                        okDifferentArchs[0] = false;
                    }
                    return cmp;
                });

                // Check if all instances of the file entry are directories
                boolean okDirectory = entry.getValue().stream().map(Map.Entry::getKey).allMatch(CpioArchiveEntry::isDirectory);

                Decorated decoratedFile = Decorated.actual(entry.getKey());
                Decorated decoratedProviders = Decorated.actual(entry.getValue().stream().map(p -> p.getValue().getFileName()).toList());

                if (okDifferentArchs[0]) {
                    pass("File {0} provided by RPMs of unique architectures: {1}",
                            decoratedFile, decoratedProviders);
                    return;
                }

                if (okDirectory) {
                    pass("Directory {0} provided by multiple RPMs: {1}",
                            decoratedFile, decoratedProviders);
                    return;
                }

                validate(Path.of(entry.getKey()), Collections.unmodifiableCollection(providers));
            }
        }
    }

    /// Validates a specific duplicate file found in multiple RPM packages.
    ///
    /// @param path         The file path being validated.
    /// @param providerRpms The collection of RPM packages providing the file.
    /// @throws Exception If an error occurs during validation.
    public abstract void validate(Path path, Collection<? extends RpmInfo> providerRpms) throws Exception;

    /// Default implementation for handling duplicate file validation.
    public static abstract class DefaultDuplicateFileValidator extends DuplicateFileValidator {

        /// Validates whether a duplicate file is allowed in multiple RPM packages.
        ///
        /// @param path         The file path being validated.
        /// @param providerRpms The collection of RPM packages providing the file.
        /// @throws Exception If an error occurs during validation.
        @Override
        public void validate(Path path, Collection<? extends RpmInfo> providerRpms) throws Exception {
            Decorated decoratedFile = Decorated.actual(path);
            Decorated decoratedProviders = Decorated.actual(List.copyOf(providerRpms));

            if (allowedDuplicateFile(path, Collections.unmodifiableCollection(providerRpms))) {
                pass("Allowed duplicate file {0} provided by multiple RPMs: {1}",
                        decoratedFile, decoratedProviders);
            } else {
                fail("File {0} provided by multiple RPMs: {1}",
                        decoratedFile, decoratedProviders);
            }
        }

        /// Determines whether a duplicate file is allowed in multiple RPM packages.
        ///
        /// @param path         The file path being checked.
        /// @param providerRpms The collection of RPM packages providing the file.
        /// @return `true` if the duplicate file is allowed, `false`
        ///         otherwise.
        /// @throws Exception If an error occurs during validation.
        public abstract boolean allowedDuplicateFile(Path path, Collection<? extends RpmInfo> providerRpms) throws Exception;
    }
}
