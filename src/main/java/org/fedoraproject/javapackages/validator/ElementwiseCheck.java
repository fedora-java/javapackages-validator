package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public abstract class ElementwiseCheck<Config> extends Check<Config> {
    private Predicate<RpmPathInfo> filter = rpm -> true;

    protected ElementwiseCheck(Config config) {
        super(config);
    }

    protected void setFilter(Predicate<RpmPathInfo> filter) {
        this.filter = filter;
    }

    abstract protected Collection<String> check(RpmPathInfo rpm) throws IOException;

    public final Collection<String> check(Path rpmPath) throws IOException {
        return check(new RpmPackageInfo(rpmPath));
    }

    @Override
    public final Collection<String> check(Iterator<? extends RpmPathInfo> testRpms) throws IOException {
        var result = new ArrayList<String>(0);

        while (testRpms.hasNext()) {
            RpmPathInfo next = testRpms.next();
            if (filter.test(next)) {
                result.addAll(check(next));
            }
        }

        return result;
    }
}
