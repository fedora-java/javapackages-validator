package org.fedoraproject.javapackages.validator.util;

import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.DefaultValidator;
import org.fedoraproject.javapackages.validator.spi.Decorated;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public abstract class ElementwiseValidator extends DefaultValidator {
    private Predicate<RpmInfo> filter;

    protected ElementwiseValidator() {
        this(_ -> true);
    }

    protected ElementwiseValidator(Predicate<RpmInfo> filter) {
        super();
        this.filter = filter;
    }

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

    public abstract void validate(RpmPackage rpm) throws Exception;
}
