package org.fedoraproject.javapackages.validator.validators;

import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.TestResult;
import org.fedoraproject.javapackages.validator.util.ConcurrentValidator;
import org.fedoraproject.javapackages.validator.util.ElementwiseResultBuilder;

import io.kojan.javadeptools.rpm.RpmDependency;
import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public class AttributeProvidesValidator extends ConcurrentValidator {
    @Override
    public String getTestName() {
        return "/java/attributes/provides";
    }

    public AttributeProvidesValidator() {
        super(Predicate.not(RpmInfo::isSourcePackage));
    }

    @Override
    protected ElementwiseResultBuilder spawnValidator() {
        return new ElementwiseResultBuilder() {
            @Override
            public void validate(RpmPackage rpm) throws Exception {
                for (RpmDependency provide : rpm.getInfo().getProvides()) {
                    if (provide.getName().startsWith("mvn(") && provide.getName().endsWith(")")) {
                        if (provide.getVersion().getVersion().isEmpty()) {
                            fail("{0}: Provide field {1} does not contain version",
                                    Decorated.rpm(rpm), Decorated.actual(provide));
                        } else if (provide.getVersion().getVersion().chars().noneMatch(Character::isDigit)) {
                            fail("{0}: The provided version of field {1} does not contain a number",
                                    Decorated.rpm(rpm), Decorated.actual(provide));
                        }
                    }
                }

                if (!TestResult.fail.equals(getResult())) {
                    pass("{0}: RPM attribute Provides - ok", Decorated.rpm(rpm));
                }
            }
        };
    }
}
