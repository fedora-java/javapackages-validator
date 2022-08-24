package org.fedoraproject.javapackages.validator.checks;

import static org.fedoraproject.javapackages.validator.TestCommon.assertFailOne;
import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.junit.jupiter.api.Test;

public class DuplicateFileCheckTest {
    private static final Path DUPLICATE_FILE1_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/duplicate-file1-1-1.noarch.rpm"));
    private static final Path DUPLICATE_FILE2_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/duplicate-file2-1-1.noarch.rpm"));

    @Test
    void testIllegalDuplicateFile() throws IOException {
        var result = new DuplicateFileCheck().check(null, TestCommon.iteratorFrom(Stream.of(
                DUPLICATE_FILE1_RPM, DUPLICATE_FILE2_RPM)));
        assertFailOne(result);
    }

    @Test
    void testAllowedDuplicateFile() throws IOException {
        var result = new DuplicateFileCheck().check((filename, providerRpms) -> true,
                TestCommon.iteratorFrom(Stream.of(DUPLICATE_FILE1_RPM, DUPLICATE_FILE2_RPM)));
        assertPass(result);
    }
}
