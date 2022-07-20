package org.fedoraproject.javapackages.validator.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        var result = new DuplicateFileCheck().check(TestCommon.iteratorFrom(Stream.of(
                DUPLICATE_FILE1_RPM, DUPLICATE_FILE2_RPM)));
        assertEquals(1, result.size());
    }

    @Test
    void testAllowedDuplicateFile() throws IOException {
        var result = new DuplicateFileCheck((filename, providerRpms) -> true).check(
                TestCommon.iteratorFrom(Stream.of(DUPLICATE_FILE1_RPM, DUPLICATE_FILE2_RPM)));
        assertEquals(0, result.size());
    }
}
