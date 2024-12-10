package org.fedoraproject.javapackages.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;

import org.fedoraproject.javapackages.validator.spi.LogEvent;
import org.fedoraproject.javapackages.validator.spi.Result;
import org.fedoraproject.javapackages.validator.spi.TestResult;

import io.kojan.javadeptools.rpm.RpmPackage;


public class TestCommon {
    public static final Path RPMBUILD_PATH_PREFIX = Path.of("src/test/resources/rpmbuild");
    public static final Path RPM_PATH_PREFIX = RPMBUILD_PATH_PREFIX.resolve(Path.of("RPMS"));
    public static final Path SRPM_PATH_PREFIX = RPMBUILD_PATH_PREFIX.resolve(Path.of("SRPMS"));

    public static Iterable<RpmPackage> fromPaths(Path... paths) {
        var result = new ArrayList<RpmPackage>(paths.length);
        for (var path : paths) {
            try {
                result.add(new RpmPackage(path));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return result;
    }

    public static void assertSkip(Result result) {
        assertEquals(TestResult.skip, result.getResult(), "expected result: SKIP");
    }

    public static void assertPass(Result result) {
        assertEquals(TestResult.pass, result.getResult(), "expected result: PASS");
    }

    public static void assertInfo(Result result) {
        assertEquals(TestResult.info, result.getResult(), "expected result: INFO");
    }

    public static void assertFailOne(Result result) {
        assertEquals(TestResult.fail, result.getResult(), "expected result: FAIL");
        int count = 0;
        for (var entry : result) {
            if (LogEvent.fail.equals(entry.kind())) {
                ++count;
            }
        }
        assertEquals(1, count);
    }

    public static void assertError(Result result) {
        assertEquals(TestResult.error, result.getResult(), "expected result: ERROR");
    }
}
