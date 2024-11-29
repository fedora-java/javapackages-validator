package org.fedoraproject.javapackages.validator.validators;

import java.util.List;

import org.fedoraproject.javapackages.validator.spi.Validator;
import org.fedoraproject.javapackages.validator.spi.ValidatorFactory;

public class DefaultValidatorFactory implements ValidatorFactory {
    @Override
    public List<Validator> getValidators() {
        return List.of(new Validator[] {
                new AttributeBuildRequiresValidator(),
                new AttributeProvidesValidator(),
                new AttributeRequiresValidator(),
                new BytecodeVersionValidator(),
                new JpmsProvidesValidator(),
                new JavadocNoarchValidator(),
                new JavaExclusiveArchValidator(),
                new MavenMetadataValidator(),
                new NoBootstrapValidator(),
                new NVRJarMetadataValidator(),
        });
    }
}
