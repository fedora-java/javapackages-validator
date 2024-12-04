package org.fedoraproject.javapackages.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fedoraproject.javapackages.validator.spi.Result;
import org.fedoraproject.javapackages.validator.spi.Validator;
import org.fedoraproject.javapackages.validator.util.ConcurrentResultBuilder;

import io.kojan.javadeptools.rpm.RpmPackage;

public abstract class DefaultValidator extends ConcurrentResultBuilder implements Validator {
    private List<String> args = null;

    @Override
    public Result validate(Iterable<RpmPackage> rpms, List<String> args) {
        if (args != null) {
            this.args = Collections.unmodifiableList(new ArrayList<>(args));
        }
        validateNoexcept(rpms);
        return build();
    }

    protected List<String> getArgs() {
        return args;
    }

    protected void validateNoexcept(Iterable<RpmPackage> rpms) {
        try {
            validate(rpms);
        } catch (Exception ex) {
            error(ex);
        }
    }

    protected abstract void validate(Iterable<RpmPackage> rpms) throws Exception;
}
