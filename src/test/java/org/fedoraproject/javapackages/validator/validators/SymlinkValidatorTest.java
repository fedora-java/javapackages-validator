package org.fedoraproject.javapackages.validator.validators;

import static org.fedoraproject.javapackages.validator.TestCommon.assertFailOne;
import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.junit.jupiter.api.Test;

public class SymlinkValidatorTest {
    private static final Path DANGLING_SYMLINK_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/dangling-symlink-1-1.noarch.rpm"));
    private static final Path VALID_SYMLINK_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/valid-symlink-1-1.noarch.rpm"));

    @Test
    void testDanglingSymlink() throws Exception {
        var validator = new SymlinkValidator();
        validator.validate(TestCommon.fromPaths(DANGLING_SYMLINK_RPM));
        assertFailOne(validator.build());
    }

    @Test
    void testValidSymlink() throws Exception {
        var validator = new SymlinkValidator();
        validator.validate(TestCommon.fromPaths(VALID_SYMLINK_RPM));
        assertPass(validator.build());
    }
}
