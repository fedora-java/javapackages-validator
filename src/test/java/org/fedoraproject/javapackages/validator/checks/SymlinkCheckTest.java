package org.fedoraproject.javapackages.validator.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.fedoraproject.javapackages.validator.config.SymlinkConfig;
import org.junit.jupiter.api.Test;

public class SymlinkCheckTest {
    private static final Path DANGLING_SYMLINK_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/dangling-symlink-1-1.noarch.rpm"));
    private static final Path VALID_SYMLINK_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/valid-symlink-1-1.noarch.rpm"));

    @Test
    void testDanglingSymlink() throws IOException {
        var result = new SymlinkCheck(new SymlinkConfig.Envroot(Paths.get("/"))).check(
                TestCommon.iteratorFrom(Stream.of(DANGLING_SYMLINK_RPM)));
        assertEquals(1, result.size());
    }

    @Test
    void testValidSymlink() throws IOException {
        var result = new SymlinkCheck(new SymlinkConfig.Envroot(Paths.get("/"))).check(
                TestCommon.iteratorFrom(Stream.of(VALID_SYMLINK_RPM)));
        assertEquals(0, result.size());
    }
}
