package org.fedoraproject.javapackages.validator.validators;


import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.TestResult;
import org.fedoraproject.javapackages.validator.util.ConcurrentValidator;
import org.fedoraproject.javapackages.validator.util.ElementwiseResultBuilder;

import io.kojan.javadeptools.rpm.RpmDependency;
import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public class AttributeRequiresValidator extends ConcurrentValidator {
    @Override
    public String getTestName() {
        return "/java/attributes/requires";
    }

    public AttributeRequiresValidator() {
        super(Predicate.not(RpmInfo::isSourcePackage));
    }

    @Override
    protected ElementwiseResultBuilder spawnValidator() {
        return new ElementwiseResultBuilder() {
            @Override
            public void validate(RpmPackage rpm) throws Exception {
                boolean jpFilesystem = false;

                for (RpmDependency require : rpm.getInfo().getRequires()) {
                    jpFilesystem |= require.getName().equals("javapackages-filesystem");
                    if (require.getName().startsWith("mvn(") && require.getName().endsWith(")")) {
                        if (require.getVersion().getVersion().chars().noneMatch(Character::isDigit)) {
                            fail("{0}: The required version of field {1} does not contain a number",
                                    Decorated.rpm(rpm), Decorated.actual(require));
                        }
                    }
                }

                if (!jpFilesystem) {
                    // fail("{0}: Requires field does not contain javapackages-filesystem", Decorated.rpm(rpm));
                }

                if (!TestResult.fail.equals(getResult())) {
                    pass("{0}: RPM attribute Requires ok", Decorated.rpm(rpm));
                }
            }
        };
    }
}
