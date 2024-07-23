package org.fedoraproject.javapackages.validator;

import java.util.ArrayList;
import java.util.List;

import org.fedoraproject.javapackages.validator.spi.ResultBuilder;
import org.fedoraproject.javapackages.validator.spi.Validator;
import org.fedoraproject.javapackages.validator.spi.ValidatorFactory;

import io.kojan.javadeptools.rpm.RpmPackage;

public class TestFactory implements ValidatorFactory {

    static List<Validator> validators = new ArrayList<>();

    @Override
    public List<Validator> getValidators() {
        return new ArrayList<>(validators);
    }

}

interface AnonymousValidator {
    void validate(Iterable<RpmPackage> rpms, ResultBuilder rb) throws Exception;
}

class TestValidator extends DefaultValidator {

    final String name;
    final AnonymousValidator delegate;

    TestValidator(String name, AnonymousValidator delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    @Override
    public String getTestName() {
        return name;
    }

    @Override
    protected void validate(Iterable<RpmPackage> rpms) throws Exception {
        delegate.validate(rpms, this);
    }

}