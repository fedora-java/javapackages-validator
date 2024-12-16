package org.fedoraproject.javapackages.validator.validators;

import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.util.Common;
import org.fedoraproject.javapackages.validator.util.ConcurrentValidator;
import org.fedoraproject.javapackages.validator.util.ElementwiseResultBuilder;

import io.kojan.javadeptools.rpm.RpmPackage;

public class JavadocNoarchValidator extends ConcurrentValidator {
    @Override
    public String getTestName() {
        return "/java/javadoc_noarch";
    }

    public JavadocNoarchValidator() {
        super(rpm -> !rpm.isSourcePackage() && rpm.getName().equals(Common.getPackageName(rpm) + "-javadoc"));
    }

    @Override
    protected ElementwiseResultBuilder spawnValidator() {
        return new ElementwiseResultBuilder() {
            @Override
            public void validate(RpmPackage rpm) throws Exception {
                if (!rpm.getInfo().getArch().equals("noarch")) {
                    fail("{0} is a javadoc package but its architecture is {1}",
                            Decorated.rpm(rpm),
                            Decorated.actual(rpm.getInfo().getArch()));
                } else {
                    pass("{0} is a javadoc package and its architecture is {1}",
                            Decorated.rpm(rpm),
                            Decorated.actual("noarch"));
                }
            }
        };
    }
}
