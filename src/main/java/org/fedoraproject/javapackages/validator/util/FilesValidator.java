package org.fedoraproject.javapackages.validator.util;

import java.nio.file.Path;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.fedoraproject.javapackages.validator.spi.Decorated;

import io.kojan.javadeptools.rpm.RpmArchiveInputStream;
import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

/// Abstract class for validating files within an RPM package.
///
/// This validator processes the files inside an RPM package and checks whether
/// they meet the defined validation rules.
public abstract class FilesValidator extends ElementwiseValidator {

    /// Determines whether a file inside the RPM package is allowed.
    ///
    /// @param rpm  The RPM package metadata.
    /// @param path The file path inside the RPM package.
    /// @return `true` if the file is allowed, `false` otherwise.
    /// @throws Exception If an error occurs during validation.
    public abstract boolean allowedFile(RpmInfo rpm, Path path) throws Exception;

    /// Validates the files inside an RPM package.
    ///
    /// @param rpm The RPM package to validate.
    /// @throws Exception If an error occurs while processing the package.
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
