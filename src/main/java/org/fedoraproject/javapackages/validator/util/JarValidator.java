package org.fedoraproject.javapackages.validator.util;

import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.spi.Decoration;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

/// Abstract class for validating JAR files within an RPM package.
///
/// This validator processes RPM packages and applies validation rules to JAR
/// files contained within them. It extends [ElementwiseValidator] and
/// implements [RpmJarConsumer] to facilitate JAR file validation.
public abstract class JarValidator extends ElementwiseValidator implements RpmJarConsumer {

    /// Decoration style for highlighting JAR paths.
    public static final Decoration DECORATION_JAR = new Decoration(Decoration.Color.blue, Decoration.Modifier.bright);

    /// Constructs a JAR validator, which excludes source RPM packages.
    protected JarValidator() {
        super(Predicate.not(RpmInfo::isSourcePackage));
    }

    /// Constructs a JAR validator with a custom filtering condition.
    ///
    /// @param filter A predicate used to determine whether validation should be
    ///               applied.
    protected JarValidator(Predicate<RpmInfo> filter) {
        super(filter);
    }

    /// Validates an RPM package by scanning for JAR files and applying validation
    /// rules.
    ///
    /// @param rpm The RPM package to validate.
    /// @throws Exception If an error occurs during validation.
    @Override
    public void validate(RpmPackage rpm) throws Exception {
        accept(rpm);
    }
}
