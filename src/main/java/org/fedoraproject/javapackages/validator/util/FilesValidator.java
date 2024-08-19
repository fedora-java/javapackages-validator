package org.fedoraproject.javapackages.validator.util;

import java.nio.file.Path;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.fedoraproject.javapackages.validator.spi.Decorated;

import io.kojan.javadeptools.rpm.RpmArchiveInputStream;
import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public abstract class FilesValidator extends ElementwiseValidator {
    public abstract boolean allowedFile(RpmInfo rpm, Path path) throws Exception;

    @Override
    public void validate(RpmPackage rpm) throws Exception {
        try (var is = new RpmArchiveInputStream(rpm.getPath())) {
            boolean pass = true;
            for (CpioArchiveEntry rpmEntry; (rpmEntry = is.getNextEntry()) != null;) {
                Path entryName = Common.getEntryPath(rpmEntry);

                if (!allowedFile(rpm.getInfo(), entryName)) {
                    pass = false;
                    fail("{0}: Illegal file: {1}",
                            Decorated.rpm(rpm),
                            Decorated.actual(entryName));
                }
            }

            if (pass) {
                pass("{0}: Listed files - ok", Decorated.rpm(rpm));
            }
        }
    }
}
