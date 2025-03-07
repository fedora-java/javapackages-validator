package org.fedoraproject.javapackages.validator;

import java.util.ArrayList;
import java.util.List;

import org.fedoraproject.javapackages.validator.spi.Validator;
import org.fedoraproject.javapackages.validator.spi.ValidatorFactory;

import io.kojan.javadeptools.rpm.RpmPackage;

/// Factory class for creating and managing validators.
public class TestFactory implements ValidatorFactory {

    /// List of registered validators.
    static List<Validator> validators = new ArrayList<>();

    /// Retrieves a list of validators.
    ///
    /// @return a new list containing all registered validators.
    @Override
    public List<Validator> getValidators() {
        return new ArrayList<>(validators);
    }
}

/// Functional interface representing an anonymous validator.
interface AnonymousValidator {

    /// Validates a collection of RPM packages using the given default validator.
    ///
    /// @param rpms the iterable collection of RPM packages to validate.
    /// @param rb   the default validator used for validation.
    /// @throws Exception if any validation error occurs.
    void validate(Iterable<RpmPackage> rpms, DefaultValidator rb) throws Exception;
}

/// A test validator that extends the DefaultValidator and delegates validation to an AnonymousValidator.
class TestValidator extends DefaultValidator {

    /// Name of the test validator.
    final String name;

    /// Delegate validator that performs the actual validation.
    final AnonymousValidator delegate;

    /// Constructs a TestValidator with a given name and delegate validator.
    ///
    /// @param name     the name of the validator.
    /// @param delegate the anonymous validator to delegate validation to.
    TestValidator(String name, AnonymousValidator delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    /// Retrieves the name of the test validator.
    ///
    /// @return the name of the test.
    @Override
    public String getTestName() {
        return name;
    }

    /// Validates a collection of RPM packages by delegating to the anonymous validator.
    ///
    /// @param rpms the iterable collection of RPM packages to validate.
    /// @throws Exception if any validation error occurs.
    @Override
    protected void validate(Iterable<RpmPackage> rpms) throws Exception {
        delegate.validate(rpms, this);
    }
}
