package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import org.fedoraproject.javadeptools.rpm.RpmInfo;

public abstract class ElementwiseCheck<Config> extends Check<Config> {
    private Predicate<RpmInfo> filter = rpm -> true;

    protected ElementwiseCheck(Config config) {
        super(config);
    }

    protected void setFilter(Predicate<RpmInfo> filter) {
        this.filter = filter;
    }

    abstract protected Collection<String> check(RpmInfo rpm) throws IOException;

    public final Collection<String> check(Path rpmPath) throws IOException {
        return check(new RpmInfo(rpmPath));
    }

    @Override
    public final Collection<String> check(Iterator<RpmInfo> testRpms) throws IOException {
        var result = new ArrayList<String>(0);

        while (testRpms.hasNext()) {
            RpmInfo next = testRpms.next();
            if (filter.test(next)) {
                result.addAll(check(next));
            }
        }

        return result;
    }
}
