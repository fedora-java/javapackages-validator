package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

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
        var files = new TreeMap<String, ArrayList<Path>>();

        while (testRpms.hasNext()) {
            RpmPathInfo rpm = testRpms.next();
            if (!new RpmInfo(rpm.getPath()).isSourcePackage()) {
                for (var pair : Common.rpmFilesAndSymlinks(rpm.getPath()).entrySet()) {
                    files.computeIfAbsent(pair.getKey().getName().substring(1), key -> new ArrayList<Path>()).add(rpm.getPath());
                }
            }
        }

        for (var entry : files.entrySet()) {
            if (entry.getValue().size() > 1) {
                var providers = new ArrayList<RpmPackageInfo>(entry.getValue().size());
                for (var providerRpmPath : entry.getValue()) {
                    providers.add(new RpmPackageInfo(providerRpmPath));
                }
                var ok = new Boolean[] {true};
                providers.sort((lhs, rhs) -> {
                    int cmp = lhs.getArch().compareTo(rhs.getArch());
                    if (cmp == 0 || lhs.getArch().equals("noarch") || rhs.getArch().equals("noarch")) {
                        ok[0] = false;
                    }
                    return cmp;
                });
                String decoratedProviders = entry.getValue().stream().map(path -> Main.getDecorator().decorate(path, Decoration.bright_red)).toList().toString();
                if (ok[0]) {
                    getLogger().pass("File {0} provided by RPMs of unique architectures: {1}",
                            Main.getDecorator().decorate(entry.getKey(), Decoration.bright_cyan),
                            decoratedProviders);
                } else if (getConfig() != null && getConfig().allowedDuplicateFile(entry.getKey(), providers)) {
                    getLogger().pass("Allowed duplicate file {0} provided by multiple RPMs: {1}",
                            Main.getDecorator().decorate(entry.getKey(), Decoration.bright_cyan),
                            decoratedProviders);
                } else {
                    result.add(failMessage("File {0} provided by multiple RPMs: {1}",
                            Main.getDecorator().decorate(entry.getKey(), Decoration.bright_cyan),
                            decoratedProviders));
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new DuplicateFileCheck().executeCheck(args));
    }
}
