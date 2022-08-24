package org.fedoraproject.javapackages.validator.checks;

import static org.fedoraproject.javapackages.validator.TestCommon.assertFailOne;
import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.junit.jupiter.api.Test;

public class JavaExclusiveArchCheckTest {
    private static final Path EA_ARCHFUL = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-archful-1-1.src.rpm"));
    private static final Path EA_NOARCH = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-noarch-1-1.src.rpm"));
    private static final Path EA_ARCHFUL_MISSING = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-archful-missing-1-1.src.rpm"));
    private static final Path EA_ARCHFUL_NOARCH = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-archful-noarch-1-1.src.rpm"));

    @Test
    public void testAllowedExclusiveArchArchful() throws IOException {
        var result = new JavaExclusiveArchCheck().check(null, TestCommon.iteratorFrom(Stream.of(EA_ARCHFUL)));
        assertPass(result);
    }

    @Test
    public void testAllowedExclusiveArchNoarch() throws IOException {
        var result = new JavaExclusiveArchCheck().check(null, TestCommon.iteratorFrom(Stream.of(EA_NOARCH)));
        assertPass(result);
    }

    @Test
    public void testExclusiveArchMissingNoarch() throws IOException {
        var result = new JavaExclusiveArchCheck().check(null, TestCommon.iteratorFrom(Stream.of(EA_ARCHFUL_MISSING)));
        assertFailOne(result);
    }

    @Test
    public void testIllegalExclusiveArchAdditionalNoarch() throws IOException {
        var result = new JavaExclusiveArchCheck().check(null, TestCommon.iteratorFrom(Stream.of(EA_ARCHFUL_NOARCH)));
        assertFailOne(result);
    }
}
