package org.fedoraproject.javapackages.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fedoraproject.javapackages.validator.spi.Result;
import org.fedoraproject.javapackages.validator.spi.ResultBuilder;
import org.fedoraproject.javapackages.validator.spi.Validator;

import io.kojan.javadeptools.rpm.RpmPackage;

/**
 * An abstract implementation of a {@code Validator} that extends
 * {@code ResultBuilder}. This class provides a base for validation logic with
 * argument handling and error management.
 */
public abstract class DefaultValidator extends ResultBuilder implements Validator {

    /** Immutable list of arguments passed to the validator. */
    private List<String> args = null;

    /**
     * Validates a collection of RPM packages using the provided arguments. Captures
     * any exceptions and records them as errors.
     *
     * @param rpms the iterable collection of {@code RpmPackage} instances to
     *             validate
     * @param args the list of arguments for validation
     * @return the validation {@code Result}
     */
    @Override
    public Result validate(Iterable<RpmPackage> rpms, List<String> args) {
        if (args != null) {
            this.args = Collections.unmodifiableList(new ArrayList<>(args));
        }
        try {
            validate(rpms);
        } catch (Exception ex) {
            error(ex);
        }

        return build();
    }

    /**
     * Returns the list of arguments provided during validation.
     *
     * @return an immutable list of arguments, or {@code null} if none were provided
     */
    protected List<String> getArgs() {
        return args;
    }

    /**
     * Abstract method to be implemented by subclasses for performing validation on
     * the given RPM packages.
     *
     * @param rpms the iterable collection of {@code RpmPackage} instances to
     *             validate
     * @throws Exception if an error occurs during validation
     */
    protected abstract void validate(Iterable<RpmPackage> rpms) throws Exception;
}
