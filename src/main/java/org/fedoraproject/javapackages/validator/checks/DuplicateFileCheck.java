package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.lang3.tuple.Pair;
import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.Check;
import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.Main;
import org.fedoraproject.javapackages.validator.RpmPackageInfo;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;
import org.fedoraproject.javapackages.validator.config.DuplicateFileConfig;

/**
 * Ignores source rpms.
 */
public class DuplicateFileCheck extends Check<DuplicateFileConfig> {
    public DuplicateFileCheck() {
        this(null);
    }

    public DuplicateFileCheck(DuplicateFileConfig config) {
        super(DuplicateFileConfig.class, config);
    }

    @Override
    public Collection<String> check(Iterator<? extends RpmPathInfo> testRpms) throws IOException {
        var result = new ArrayList<String>(0);

        // The union of file paths present in all RPM files mapped to the RPM file names they are present in
        var files = new TreeMap<String, ArrayList<Pair<CpioArchiveEntry, Path>>>();

        while (testRpms.hasNext()) {
            RpmPathInfo rpm = testRpms.next();
            if (!new RpmInfo(rpm.getPath()).isSourcePackage()) {
                for (var pair : Common.rpmFilesAndSymlinks(rpm.getPath()).entrySet()) {
                    files.computeIfAbsent(Common.getEntryPath(pair.getKey()).toString(), key -> new ArrayList<>())
                        .add(Pair.of(pair.getKey(), rpm.getPath()));
                }
            }
        }

        for (var entry : files.entrySet()) {
            if (entry.getValue().size() > 1) {
                var providers = new ArrayList<RpmPackageInfo>(entry.getValue().size());
                for (var providerPair : entry.getValue()) {
                    providers.add(new RpmPackageInfo(providerPair.getValue()));
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

                String decoratedFile = Main.getDecorator().decorate(entry.getKey(), Decoration.bright_cyan);
                String decoratedProviders = entry.getValue().stream().map(pair -> Main.getDecorator().decorate(pair.getValue(), Decoration.bright_red)).toList().toString();
                if (okDifferentArchs[0]) {
                    getLogger().pass("File {0} provided by RPMs of unique architectures: {1}",
                            decoratedFile, decoratedProviders);
                } else if (okDirectory) {
                    getLogger().pass("File {0} is a directory provided by multiple RPMs: {1}",
                            decoratedFile, decoratedProviders);
                } else if (getConfig() != null && getConfig().allowedDuplicateFile(entry.getKey(), providers)) {
                    getLogger().pass("Allowed duplicate file {0} provided by multiple RPMs: {1}",
                            decoratedFile, decoratedProviders);
                } else {
                    result.add(failMessage("File {0} provided by multiple RPMs: {1}",
                            decoratedFile, decoratedProviders));
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new DuplicateFileCheck().executeCheck(args));
    }
}
