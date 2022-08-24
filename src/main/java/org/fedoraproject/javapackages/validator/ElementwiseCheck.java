package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;

public abstract class ElementwiseCheck<Config> extends Check<Config> {
    private Predicate<RpmPathInfo> filter = rpm -> true;

    protected ElementwiseCheck(Class<Config> configClass) {
        super(configClass);
    }

    protected ElementwiseCheck<?> setFilter(Predicate<RpmPathInfo> filter) {
        this.filter = filter;
        return this;
    }

    abstract protected CheckResult check(Config config, RpmPathInfo rpm) throws IOException;

    @Override
    public final CheckResult check(Config config, Iterator<RpmPathInfo> rpmIt) throws IOException {
        var result = new CheckResult();

        while (rpmIt.hasNext()) {
            RpmPathInfo rpm = rpmIt.next();
            if (filter.test(rpm)) {
                result.combineWith(check(config, rpm));
            } else {
                Decoration[] decorations = {Decoration.bright_yellow};
                getLogger().debug("{0}: filtered out {1}",
                        Decorated.custom(getClass().getSimpleName(), decorations),
                        Decorated.rpm(rpm.getPath()));
            }
        }

        return result;
    }
}
