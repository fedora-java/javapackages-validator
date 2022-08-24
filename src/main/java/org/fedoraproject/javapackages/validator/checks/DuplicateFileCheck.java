package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.utils.Iterators;
import org.apache.commons.lang3.tuple.Pair;
import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.Check;
import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.Decorated;
import org.fedoraproject.javapackages.validator.CheckResult;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.config.DuplicateFileConfig;

/**
 * Ignores source rpms.
 */
public class DuplicateFileCheck extends Check<DuplicateFileConfig> {
    public DuplicateFileCheck() {
        super(DuplicateFileConfig.class);
    }

    @Override
    public CheckResult check(DuplicateFileConfig config, Iterator<RpmPathInfo> rpmIt) throws IOException {
        var testRpms = new ArrayList<RpmPathInfo>();
        Iterators.addAll(testRpms, rpmIt);

        var result = new CheckResult();

        // The union of file paths present in all RPM files mapped to the RPMs they are present in
        var files = new TreeMap<String, ArrayList<Pair<CpioArchiveEntry, RpmPathInfo>>>();

        for (RpmPathInfo rpm : testRpms) {
            if (!new RpmPathInfo(rpm.getPath()).isSourcePackage()) {
                for (var pair : Common.rpmFilesAndSymlinks(rpm.getPath()).entrySet()) {
                    files.computeIfAbsent(Common.getEntryPath(pair.getKey()).toString(), key -> new ArrayList<>())
                        .add(Pair.of(pair.getKey(), rpm));
                }
            }
        }

        for (var entry : files.entrySet()) {
            if (entry.getValue().size() > 1) {
                var providers = new ArrayList<RpmInfo>(entry.getValue().size());
                for (var providerPair : entry.getValue()) {
                    providers.add(providerPair.getValue());
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
                boolean okDirectory = entry.getValue().stream().map(Pair::getKey).allMatch(CpioArchiveEntry::isDirectory);

                Decorated decoratedFile = Decorated.actual(entry.getKey());
                Decorated decoratedProviders = Decorated.list(entry.getValue().stream().map(pair -> Decorated.rpm(pair.getValue())).toList());
                if (okDifferentArchs[0]) {
                    getLogger().info("File {0} provided by RPMs of unique architectures: {1}",
                            decoratedFile, decoratedProviders);
                } else if (okDirectory) {
                    getLogger().info("Directory {0} provided by multiple RPMs: {1}",
                            decoratedFile, decoratedProviders);
                } else if (config != null && config.allowedDuplicateFile(Paths.get(entry.getKey()), providers)) {
                    getLogger().pass("Allowed duplicate file {0} provided by multiple RPMs: {1}",
                            decoratedFile, decoratedProviders);
                } else {
                    result.add("File {0} provided by multiple RPMs: {1}",
                            decoratedFile, decoratedProviders);
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new DuplicateFileCheck().executeCheck(args));
    }
}
