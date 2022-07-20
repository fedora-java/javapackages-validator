package org.fedoraproject.javapackages.validator.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.fedoraproject.javapackages.validator.config.ExclusiveArchConfig;
import org.fedoraproject.javapackages.validator.config.RpmPackage;
import org.junit.jupiter.api.Test;

public class ExclusiveArchCheckTest {
    private static final Path EA_ARCHFUL = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-archful-1-1.src.rpm"));
    private static final Path EA_NOARCH = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-noarch-1-1.src.rpm"));
    private static final Path EA_ARCHFUL_MISSING = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-archful-missing-1-1.src.rpm"));
    private static final Path EA_ARCHFUL_NOARCH = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("exclusive-arch-archful-noarch-1-1.src.rpm"));

    private static class Config implements ExclusiveArchConfig {
        @Override
        public boolean allowedExclusiveArch(RpmPackage rpm, List<String> values) {
            boolean buildNoarch = rpm.getBuildArchs().contains("noarch");
            boolean exclusiveNoarch = values.contains("noarch");

            return buildNoarch == exclusiveNoarch;
        }
    }

    @Test
    public void testAllowedExclusiveArchArchful() throws IOException {
        var result = new ExclusiveArchCheck(new Config()).check(TestCommon.iteratorFrom(Stream.of(EA_ARCHFUL)));
        assertEquals(0, result.size());
    }

    @Test
    public void testAllowedExclusiveArchNoarch() throws IOException {
        var result = new ExclusiveArchCheck(new Config()).check(TestCommon.iteratorFrom(Stream.of(EA_NOARCH)));
        assertEquals(0, result.size());
    }

    @Test
    public void testExclusiveArchMissingNoarch() throws IOException {
        var result = new ExclusiveArchCheck(new Config()).check(TestCommon.iteratorFrom(Stream.of(EA_ARCHFUL_MISSING)));
        assertEquals(1, result.size());
    }

    @Test
    public void testIllegalExclusiveArchAdditionalNoarch() throws IOException {
        var result = new ExclusiveArchCheck(new Config()).check(TestCommon.iteratorFrom(Stream.of(EA_ARCHFUL_NOARCH)));
        assertEquals(1, result.size());
    }
}
