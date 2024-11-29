package org.fedoraproject.javapackages.validator.util;

import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.DefaultValidator;
import org.fedoraproject.javapackages.validator.spi.Decorated;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

/// An abstract validator that applies validation on individual
/// [RpmPackage] instances while allowing filtering based on
/// [RpmInfo].
///
///
/// This class extends [DefaultValidator] and iterates over a collection of
/// RPM packages, applying validation to each package that passes the specified
/// filter.
public abstract class ElementwiseValidator extends DefaultValidator {
    /// A predicate used to filter which RPM packages should be validated.
    private Predicate<RpmInfo> filter;

    /// Constructs an `ElementwiseValidator` with a default filter that allows
    /// all RPMs.
    protected ElementwiseValidator() {
        this(_ -> true);
    }

    /// Constructs an `ElementwiseValidator` with a custom filter.
    ///
    /// @param filter A predicate to determine which RPMs should be validated.
    protected ElementwiseValidator(Predicate<RpmInfo> filter) {
        super();
        this.filter = filter;
    }

    /// Validates an iterable collection of RPM packages. Only packages that pass the
    /// filter will be validated. Others will be skipped with a log message.
    ///
    /// @param rpms The iterable collection of [RpmPackage] instances to be
    ///             validated.
    /// @throws Exception If an error occurs during validation.
    @Override
    public void validate(Iterable<RpmPackage> rpms) throws Exception {
        for (var rpm : rpms) {
            if (filter.test(rpm.getInfo())) {
                validate(rpm);
            } else {
                skip("{0} filtered out {1}",
                        Decorated.struct(getClass().getCanonicalName()),
                        Decorated.rpm(rpm));
            }
        }
    }

    /// Validates a single [RpmPackage]. Implementations must define the
    /// validation logic.
    ///
    /// @param rpm The RPM package to validate.
    /// @throws Exception If an error occurs during validation.
    public abstract void validate(RpmPackage rpm) throws Exception;
}
