package org.fedoraproject.javapackages.validator.util;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.DefaultValidator;
import org.fedoraproject.javapackages.validator.ThreadPool;
import org.fedoraproject.javapackages.validator.spi.Decorated;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public abstract class ElementwiseValidator extends DefaultValidator {
    private Predicate<RpmInfo> filter;

    protected ElementwiseValidator() {
        this(_ -> true);
    }

    protected ElementwiseValidator(Predicate<RpmInfo> filter) {
        super();
        this.filter = filter;
    }

    @Override
    public void validate(Iterable<RpmPackage> rpms) throws Exception {
        var futures = new ArrayList<Future<?>>();
        for (var rpm : rpms) {
            if (filter.test(rpm.getInfo())) {
                futures.add(ThreadPool.submit(() -> validateNoexcept(rpm)));
            } else {
                skip("{0} filtered out {1}",
                        Decorated.struct(getClass().getCanonicalName()),
                        Decorated.rpm(rpm));
            }
        }
        for (var future : futures) {
            future.get();
        }
    }

    public void validateNoexcept(RpmPackage rpm) {
        try {
            validate(rpm);
        } catch (Exception ex) {
            error(ex);
        }
    }

    public abstract void validate(RpmPackage rpm) throws Exception;
}
