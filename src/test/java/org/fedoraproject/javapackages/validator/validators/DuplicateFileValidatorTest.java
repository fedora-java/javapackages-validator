package org.fedoraproject.javapackages.validator.validators;

import static org.fedoraproject.javapackages.validator.TestCommon.assertFailOne;
import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.fedoraproject.javapackages.validator.util.DuplicateFileValidator.DefaultDuplicateFileValidator;
import org.junit.jupiter.api.Test;

import io.kojan.javadeptools.rpm.RpmInfo;

public class DuplicateFileValidatorTest {
    private static final Path DUPLICATE_FILE1_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Path.of("noarch/duplicate-file1-1-1.noarch.rpm"));
    private static final Path DUPLICATE_FILE2_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Path.of("noarch/duplicate-file2-1-1.noarch.rpm"));
    private static final Path DUPLICATE_FILE3_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Path.of("noarch/duplicate-file3-1-1.noarch.rpm"));

    @Test
    void testIllegalDuplicateFile() throws Exception {
        var validator = new DefaultDuplicateFileValidator() {
            @Override
            public String getTestName() {
                return null;
            }
            @Override
            public boolean allowedDuplicateFile(Path path, Collection<? extends RpmInfo> providerRpms) throws IOException {
                return false;
            }
        };
        validator.validate(TestCommon.fromPaths(DUPLICATE_FILE1_RPM, DUPLICATE_FILE2_RPM));
        assertFailOne(validator.build());
    }

    @Test
    void testAllowedDuplicateFile() throws Exception {
        var validator = new DefaultDuplicateFileValidator() {
            @Override
            public String getTestName() {
                return null;
            }
            @Override
            public boolean allowedDuplicateFile(Path path, Collection<? extends RpmInfo> providerRpms) throws IOException {
                return true;
            }
        };
        validator.validate(TestCommon.fromPaths(DUPLICATE_FILE1_RPM, DUPLICATE_FILE2_RPM));
        assertPass(validator.build());
    }

    @Test
    void testDuplicateGhost() throws Exception {
        var validator = new DefaultDuplicateFileValidator() {
            @Override
            public String getTestName() {
                return null;
            }
            @Override
            public boolean allowedDuplicateFile(Path path, Collection<? extends RpmInfo> providerRpms) throws IOException {
                return false;
            }
        };
        validator.validate(TestCommon.fromPaths(DUPLICATE_FILE1_RPM, DUPLICATE_FILE3_RPM));
        assertFailOne(validator.build());
    }
}
