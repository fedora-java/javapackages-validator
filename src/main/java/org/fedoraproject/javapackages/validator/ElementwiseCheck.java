package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

public abstract class ElementwiseCheck<Config> extends Check<Config> {
    private Predicate<RpmPathInfo> filter = rpm -> true;

    protected ElementwiseCheck(Class<Config> configClass) {
        super(configClass);
    }

    protected ElementwiseCheck<?> setFilter(Predicate<RpmPathInfo> filter) {
        this.filter = filter;
        return this;
    }

    abstract protected Collection<String> check(Config config, RpmPathInfo rpm) throws IOException;

    @Override
    public final Collection<String> check(Config config, Collection<RpmPathInfo> testRpms) throws IOException {
        var result = new ArrayList<String>(0);

        for (RpmPathInfo rpm : testRpms) {
            if (filter.test(rpm)) {
                result.addAll(check(config, rpm));
            }
        }

        return result;
    }
}
