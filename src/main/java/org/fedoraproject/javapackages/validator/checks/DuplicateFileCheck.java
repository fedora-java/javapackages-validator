package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.Check;
import org.fedoraproject.javapackages.validator.RpmFiles;
import org.fedoraproject.javapackages.validator.RpmPackageImpl;
import org.fedoraproject.javapackages.validator.config.DuplicateFileConfig;

public class DuplicateFileCheck extends Check<DuplicateFileConfig> {
    @Override
    protected Collection<String> check(List<Path> testRpms, DuplicateFileConfig config) throws IOException {
        var result = new ArrayList<String>(0);

        // The union of file paths present in all RPM files mapped to the RPM file names they are present in
        var files = new TreeMap<String, ArrayList<Path>>();

        for (var rpmPath : testRpms) {
            if (!new RpmInfo(rpmPath).isSourcePackage()) {
                for (var pair : RpmFiles.filesAndSymlinks(rpmPath).entrySet()) {
                    files.computeIfAbsent(pair.getKey().getName().substring(1), key -> new ArrayList<Path>()).add(rpmPath);
                }
            }
        }

        for (var entry : files.entrySet()) {
            if (entry.getValue().size() > 1) {
                var providers = new ArrayList<RpmPackageImpl>(entry.getValue().size());
                for (var providerRpmPath : entry.getValue()) {
                    providers.add(new RpmPackageImpl(new RpmInfo(providerRpmPath)));
                }
                if (config != null && config.allowedDuplicateFile(entry.getKey(), providers)) {
                    System.err.println(MessageFormat.format("[INFO] Allowed duplicate file {0} provided by multiple RPMs: {1}",
                            entry.getKey(), entry.getValue()));
                } else {
                    result.add(MessageFormat.format("[FAIL] File {0} provided by multiple RPMs: {1}",
                            entry.getKey(), entry.getValue()));
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new DuplicateFileCheck().executeCheck(DuplicateFileConfig.class, args));
    }
}
