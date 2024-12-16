package org.fedoraproject.javapackages.validator.validators;

import java.util.Collections;

import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.util.ConcurrentValidator;
import org.fedoraproject.javapackages.validator.util.ElementwiseResultBuilder;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public class JavaExclusiveArchValidator extends ConcurrentValidator {
    @Override
    public String getTestName() {
        return "/java/exclusive_arch";
    }

    /**
     * The expanded value of `rpm -E '%{java_arches}'` as of 18. 8. 2022 on Fedora 37
     */
    private static final String JAVA_ARCHES = "aarch64 ppc64le s390x x86_64";

    public JavaExclusiveArchValidator() {
        super(RpmInfo::isSourcePackage);
    }

    @Override
    protected ElementwiseResultBuilder spawnValidator() {
        return new ElementwiseResultBuilder() {
            @Override
            public void validate(RpmPackage rpm) throws Exception {
                var buildArchs = rpm.getInfo().getBuildArchs();
                debug("{0}: Build archs: {1}", Decorated.rpm(rpm), Decorated.actual(buildArchs));
                boolean noarch = buildArchs.equals(Collections.singletonList("noarch"));

                String expected = noarch ? JAVA_ARCHES + " noarch" : JAVA_ARCHES;
                String actual = String.join(" ", rpm.getInfo().getExclusiveArch());

                if (expected.equals(actual)) {
                    pass("{0}: ExclusiveArch with %java_arches - ok",
                            Decorated.rpm(rpm));
                } else {
                    fail("{0}: expected ExclusiveArch \"{1}\" but was \"{2}\"",
                            Decorated.rpm(rpm), Decorated.expected(expected),
                            Decorated.actual(actual));
                }
            }
        };
    }
}
