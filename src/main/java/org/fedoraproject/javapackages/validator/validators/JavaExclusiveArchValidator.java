package org.fedoraproject.javapackages.validator.validators;

import java.util.Collections;

import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.util.ElementwiseValidator;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public class JavaExclusiveArchValidator extends ElementwiseValidator {
    private final String javaArches;

    @Override
    public String getTestName() {
        return "/java/exclusive_arch";
    }

    /// The expanded value of `rpm -E '%{java_arches}'` as of 18. 8. 2022 on Fedora 37
    private static final String JAVA_ARCHES = "aarch64 ppc64le s390x x86_64";

    public JavaExclusiveArchValidator() {
        this(JAVA_ARCHES);
    }

    public JavaExclusiveArchValidator(String javaArches) {
        super(RpmInfo::isSourcePackage);
        this.javaArches = javaArches;
    }

    @Override
    public void validate(RpmPackage rpm) throws Exception {
        var buildArchs = rpm.getInfo().getBuildArchs();
        debug("{0}: Build archs: {1}", Decorated.rpm(rpm), Decorated.actual(buildArchs));
        boolean noarch = buildArchs.equals(Collections.singletonList("noarch"));

        String expected = noarch ? javaArches + " noarch" : javaArches;
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
}
