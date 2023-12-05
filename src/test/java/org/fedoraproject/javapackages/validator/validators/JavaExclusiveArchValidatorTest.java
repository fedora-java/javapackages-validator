package org.fedoraproject.javapackages.validator.validators;

import static org.fedoraproject.javapackages.validator.TestCommon.assertFailOne;
import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.junit.jupiter.api.Test;

public class JavaExclusiveArchValidatorTest {
    private static final Path EA_ARCHFUL = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-archful-1-1.src.rpm"));
    private static final Path EA_NOARCH = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-noarch-1-1.src.rpm"));
    private static final Path EA_ARCHFUL_MISSING = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-archful-missing-1-1.src.rpm"));
    private static final Path EA_ARCHFUL_NOARCH = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-archful-noarch-1-1.src.rpm"));

    @Test
    public void testAllowedExclusiveArchArchful() throws Exception {
        var validator = new JavaExclusiveArchValidator();
        validator.validate(TestCommon.fromPaths(EA_ARCHFUL));
        assertPass(validator.build());
    }

    @Test
    public void testAllowedExclusiveArchNoarch() throws Exception {
        var validator = new JavaExclusiveArchValidator();
        validator.validate(TestCommon.fromPaths(EA_NOARCH));
        assertPass(validator.build());
    }

    @Test
    public void testExclusiveArchMissingNoarch() throws Exception {
        var validator = new JavaExclusiveArchValidator();
        validator.validate(TestCommon.fromPaths(EA_ARCHFUL_MISSING));
        assertFailOne(validator.build());
    }

    @Test
    public void testIllegalExclusiveArchAdditionalNoarch() throws Exception {
        var validator = new JavaExclusiveArchValidator();
        validator.validate(TestCommon.fromPaths(EA_ARCHFUL_NOARCH));
        assertFailOne(validator.build());
    }
}
