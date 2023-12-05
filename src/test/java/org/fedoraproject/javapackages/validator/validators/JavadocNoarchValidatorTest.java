package org.fedoraproject.javapackages.validator.validators;

import static org.fedoraproject.javapackages.validator.TestCommon.assertFailOne;
import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;
import static org.fedoraproject.javapackages.validator.TestCommon.assertSkip;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.junit.jupiter.api.Test;

public class JavadocNoarchValidatorTest {
    private static final Path ARCH_ARCH_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("x86_64/javadoc-arch-arch-javadoc-1-1.x86_64.rpm"));
    private static final Path ARCH_NOARCH_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/javadoc-arch-noarch-javadoc-1-1.noarch.rpm"));
    private static final Path NOARCH_NOARCH_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/javadoc-noarch-noarch-javadoc-1-1.noarch.rpm"));
    private static final Path NON_JAVADOC_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/javadoc-noarch-noarch-1-1.noarch.rpm"));

    @Test
    void testIllegalArchfulJavadoc() throws Exception {
        var validator = new JavadocNoarchValidator();
        validator.validate(TestCommon.fromPaths(ARCH_ARCH_RPM));
        assertFailOne(validator.build());
    }

    @Test
    void testAllowedNoarchJavadocArchfulPackage() throws Exception {
        var validator = new JavadocNoarchValidator();
        validator.validate(TestCommon.fromPaths(ARCH_NOARCH_RPM));
        assertPass(validator.build());
    }

    @Test
    void testAllowedNoarchJavadocNoarchPackage() throws Exception {
        var validator = new JavadocNoarchValidator();
        validator.validate(TestCommon.fromPaths(NOARCH_NOARCH_RPM));
        assertPass(validator.build());
    }

    @Test
    void testIgnoreNonJavadoc() throws Exception {
        var validator = new JavadocNoarchValidator();
        validator.validate(TestCommon.fromPaths(NON_JAVADOC_RPM));
        assertSkip(validator.build());
    }
}
