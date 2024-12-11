package org.fedoraproject.javapackages.validator.validators;

import org.fedoraproject.javapackages.validator.util.ConcurrentValidator;
import org.fedoraproject.javapackages.validator.util.ElementwiseResultBuilder;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public class AttributeBuildRequiresValidator extends ConcurrentValidator {
    @Override
    public String getTestName() {
        return "/java/attributes/build-requires";
    }

    public AttributeBuildRequiresValidator() {
        super(RpmInfo::isSourcePackage);
    }

    @Override
    protected ElementwiseResultBuilder spawnValidator() {
        return new ElementwiseResultBuilder() {
            @Override
            public void validate(RpmPackage rpm) throws Exception {
                // TODO
            }
        };
    }
}
