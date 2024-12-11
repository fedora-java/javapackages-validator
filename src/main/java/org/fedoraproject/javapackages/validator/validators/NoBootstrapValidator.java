package org.fedoraproject.javapackages.validator.validators;

import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.util.ConcurrentValidator;
import org.fedoraproject.javapackages.validator.util.ElementwiseResultBuilder;

import io.kojan.javadeptools.rpm.RpmPackage;

public class NoBootstrapValidator extends ConcurrentValidator {
    @Override
    public String getTestName() {
        return "/no-bootstrap";
    }

    @Override
    protected ElementwiseResultBuilder spawnValidator() {
        return new ElementwiseResultBuilder() {
            @Override
            public void validate(RpmPackage rpm) throws Exception {
                Decorated suffix = Decorated.actual("~bootstrap");
                if (rpm.getInfo().getRelease().endsWith("~bootstrap")) {
                    fail("{0}: Release ends with a {1} suffix", Decorated.rpm(rpm), suffix);
                } else {
                    pass("{0}: Release does not end with a {1} suffix", Decorated.rpm(rpm), suffix);
                }
            }
        };
    }
}
