package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.Check;
import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.RpmPackageImpl;
import org.fedoraproject.javapackages.validator.config.DuplicateFileConfig;

/**
 * Ignores source rpms.
 */
public class DuplicateFileCheck extends Check<DuplicateFileConfig> {
    public DuplicateFileCheck() {
        super();
    }

    DuplicateFileCheck(DuplicateFileConfig config) {
        super(config);
    }

    @Override
    protected Collection<String> check(Iterator<RpmInfo> testRpms) throws IOException {
        var result = new ArrayList<String>(0);

        // The union of file paths present in all RPM files mapped to the RPM file names they are present in
        var files = new TreeMap<String, ArrayList<Path>>();

        while (testRpms.hasNext()) {
            RpmInfo rpm = testRpms.next();
            if (!new RpmInfo(rpm.getPath()).isSourcePackage()) {
                for (var pair : Common.rpmFilesAndSymlinks(rpm.getPath()).entrySet()) {
                    files.computeIfAbsent(pair.getKey().getName().substring(1), key -> new ArrayList<Path>()).add(rpm.getPath());
                }
            }
        }

        for (var entry : files.entrySet()) {
            if (entry.getValue().size() > 1) {
                var providers = new ArrayList<RpmPackageImpl>(entry.getValue().size());
                for (var providerRpmPath : entry.getValue()) {
                    providers.add(new RpmPackageImpl(new RpmInfo(providerRpmPath)));
                }
                if (getConfig() != null && getConfig().allowedDuplicateFile(entry.getKey(), providers)) {
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
