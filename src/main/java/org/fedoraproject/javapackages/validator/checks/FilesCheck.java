package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.fedoraproject.javadeptools.rpm.RpmArchiveInputStream;
import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.RpmPackageImpl;
import org.fedoraproject.javapackages.validator.config.FilesConfig;

public class FilesCheck extends ElementwiseCheck<FilesConfig> {
    @Override
    public Collection<String> check(RpmInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        try (var is = new RpmArchiveInputStream(rpm.getPath())) {
            boolean ok = true;
            for (CpioArchiveEntry rpmEntry; ((rpmEntry = is.getNextEntry()) != null);) {
                Path entryName = Common.getEntryPath(rpmEntry);
                if (!getConfig().allowedFile(new RpmPackageImpl(rpm), entryName)) {
                    ok = false;
                    result.add(MessageFormat.format("[FAIL] {0}: Illegal file: {1}",
                            rpm.getPath(), entryName));
                }
            }

            if (ok) {
                System.err.println(MessageFormat.format("[INFO] {0}: ok", rpm.getPath()));
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new FilesCheck().executeCheck(FilesConfig.class, args));
    }
}
