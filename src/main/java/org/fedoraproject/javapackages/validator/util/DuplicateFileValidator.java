package org.fedoraproject.javapackages.validator.util;

import java.nio.file.Path;
import java.nio.file.Paths;
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

public abstract class DuplicateFileValidator extends DefaultValidator {
    @Override
    public void validate(Iterable<RpmPackage> rpms) throws Exception {
        // The union of file paths present in all RPM files mapped to the RPM file names they are present in
        var files = new TreeMap<String, ArrayList<Map.Entry<CpioArchiveEntry, Path>>>();

        for (var rpm : rpms) {
            if (!rpm.getInfo().isSourcePackage()) {
                for (var entry : Common.rpmFilesAndSymlinks(rpm).entrySet()) {
                    files.computeIfAbsent(Common.getEntryPath(entry.getKey()).toString(), key -> new ArrayList<>())
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
                // If all providers are of different architecture (with the
                // exception of noarch), then it is ok
                providers.sort((lhs, rhs) -> {
                    int cmp = lhs.getArch().compareTo(rhs.getArch());
                    if (cmp == 0 || lhs.getArch().equals("noarch") || rhs.getArch().equals("noarch")) {
                        okDifferentArchs[0] = false;
                    }
                    return cmp;
                });

                // If the file entry is a directory in all providers, then it is ok
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

                validate(Paths.get(entry.getKey()), Collections.unmodifiableCollection(providers));
            }
        }
    }

    public abstract void validate(Path path, Collection<? extends RpmInfo> providerRpms) throws Exception;

    public static abstract class DefaultDuplicateFileValidator extends DuplicateFileValidator {
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

        public abstract boolean allowedDuplicateFile(Path path, Collection<? extends RpmInfo> providerRpms) throws Exception;
    }
}
