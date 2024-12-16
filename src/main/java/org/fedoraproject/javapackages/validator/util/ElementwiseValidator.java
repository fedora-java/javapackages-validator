package org.fedoraproject.javapackages.validator.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.DefaultValidator;
import org.fedoraproject.javapackages.validator.spi.Decorated;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public abstract class ElementwiseValidator extends DefaultValidator {
    protected final Predicate<RpmInfo> filter;

    protected ElementwiseValidator() {
        this(_ -> true);
    }

    protected ElementwiseValidator(Predicate<RpmInfo> filter) {
        super();
        this.filter = filter;
    }

    protected Iterator<RpmPackage> filter(Iterable<RpmPackage> rpms) {
        return new Iterator<RpmPackage>() {
            private Iterator<RpmPackage> it = rpms.iterator();
            private RpmPackage current = null;

            @Override
            public boolean hasNext() {
                while (it.hasNext()) {
                    current = it.next();
                    if (filter.test(current.getInfo())) {
                        return true;
                    } else {
                        skip("{0} filtered out {1}",
                                Decorated.struct(getClass().getCanonicalName()),
                                Decorated.rpm(current));
                    }
                }
                return false;
            }

            @Override
            public RpmPackage next() {
                var result = current;
                if (result == null) {
                    throw new NoSuchElementException();
                }
                current = null;
                return result;
            }
        };
    }

    @Override
    public void validate(Iterable<RpmPackage> rpms) throws Exception {
        var it = filter(rpms);
        while (it.hasNext()) {
            validate(it.next());
        }
    }

    public abstract void validate(RpmPackage rpm) throws Exception;
}
