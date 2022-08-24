package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.util.Collections;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.Check;
import org.fedoraproject.javapackages.validator.Decorated;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.CheckResult;
import org.fedoraproject.javapackages.validator.RpmPathInfo;

public class JavaExclusiveArchCheck extends ElementwiseCheck<Check.NoConfig> {
    /**
     * The expanded value of `rpm -E '%{java_arches}'` as of 18. 8. 2022 on Fedora 37
     */
    private static final String JAVA_ARCHES = "aarch64 ppc64le s390x x86_64";

    public JavaExclusiveArchCheck() {
        super(Check.NoConfig.class);
        setFilter(RpmInfo::isSourcePackage);
    }

    @Override
    protected CheckResult check(Check.NoConfig config, RpmPathInfo rpm) throws IOException {
        var result = new CheckResult();

        boolean noarch = rpm.getBuildArchs().equals(Collections.singletonList("noarch"));
        String expected = noarch ? JAVA_ARCHES + " noarch" : JAVA_ARCHES;
        String actual = String.join(" ", rpm.getExclusiveArch());

        if (expected.equals(actual)) {
            getLogger().pass("{0}: ExclusiveArch with %java_arches - ok",
                    Decorated.rpm(rpm.getPath()));
        } else {
            result.add("{0}: expected ExclusiveArch \"{1}\" but was \"{2}\"",
                    Decorated.rpm(rpm.getPath()), Decorated.expected(expected),
                    Decorated.actual(actual));
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new JavaExclusiveArchCheck().executeCheck(args));
    }
}
