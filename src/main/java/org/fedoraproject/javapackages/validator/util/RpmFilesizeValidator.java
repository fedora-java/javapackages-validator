package org.fedoraproject.javapackages.validator.util;

import java.io.OutputStream;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Locale;

import org.fedoraproject.javapackages.validator.spi.Decorated;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

/// Abstract class for validating the file size of an RPM package.
///
/// This validator checks the total file size of an RPM package and determines
/// whether it meets the allowed size criteria defined by subclasses.
public abstract class RpmFilesizeValidator extends ElementwiseValidator {

    /// Validates the file size of an RPM package.
    ///
    /// @param rpm The RPM package to validate.
    /// @throws Exception If an error occurs while reading the file size.
    @Override
    public void validate(RpmPackage rpm) throws Exception {
        long filesize = 0;
        try (var is = Files.newInputStream(rpm.getPath()); var os = OutputStream.nullOutputStream()) {
            filesize = is.transferTo(os);
        }
        Decorated formattedFilesize = Decorated.actual(NumberFormat.getInstance(Locale.ENGLISH).format(filesize));

        if (allowedFilesize(rpm.getInfo(), filesize)) {
            pass("{0}: file size is: {1} bytes", Decorated.rpm(rpm), formattedFilesize);
        } else {
            fail("{0}: file size is: {1} bytes", Decorated.rpm(rpm), formattedFilesize);
        }
    }

    /// Determines whether the given file size is allowed for an RPM package.
    ///
    /// @param rpm       The RPM package metadata.
    /// @param sizeBytes The size of the RPM package in bytes.
    /// @return `true` if the file size is allowed, `false` otherwise.
    /// @throws Exception If an error occurs during validation.
    public abstract boolean allowedFilesize(RpmInfo rpm, long sizeBytes) throws Exception;
}
