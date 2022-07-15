package org.fedoraproject.javapackages.validator.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.TestCommon;
import org.fedoraproject.javapackages.validator.config.SymlinkConfig;
import org.junit.jupiter.api.Test;

public class SymlinkCheckTest {
    private static final Path DANGLING_SYMLINK_RPM = Paths.get(TestCommon.RPM_PATH_PREFIX + "/dangling-symlink-1-1.noarch.rpm");
    private static final Path VALID_SYMLINK_RPM = Paths.get(TestCommon.RPM_PATH_PREFIX + "/valid-symlink-1-1.noarch.rpm");

    @Test
    void testDanglingSymlink() throws IOException {
        var result = new SymlinkCheck(new SymlinkConfig.Envroot(Paths.get("/")))
                .check(DANGLING_SYMLINK_RPM, new RpmInfo(DANGLING_SYMLINK_RPM));
        assertEquals(1, result.size());
    }

    @Test
    void testValidSymlink() throws IOException {
        var result = new SymlinkCheck(new SymlinkConfig.Envroot(Paths.get("/")))
                .check(VALID_SYMLINK_RPM, new RpmInfo(VALID_SYMLINK_RPM));
        assertEquals(0, result.size());
    }
}
