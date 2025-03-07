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

/// Utility class providing common test-related methods and constants.
public class TestCommon {

    /// Path prefix for RPM build resources.
    public static final Path RPMBUILD_PATH_PREFIX = Path.of("src/test/resources/rpmbuild");

    /// Path prefix for RPM package files.
    public static final Path RPM_PATH_PREFIX = RPMBUILD_PATH_PREFIX.resolve(Path.of("RPMS"));

    /// Path prefix for SRPM package files.
    public static final Path SRPM_PATH_PREFIX = RPMBUILD_PATH_PREFIX.resolve(Path.of("SRPMS"));

    /// Creates an iterable collection of `RpmPackage` instances from the given paths.
    ///
    /// @param paths the paths to create `RpmPackage` instances from.
    /// @return an iterable collection of `RpmPackage` instances.
    /// @throws UncheckedIOException if an I/O error occurs while creating an `RpmPackage`.
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

    /// Asserts that the given result has a test outcome of SKIP.
    ///
    /// @param result the test result to check.
    public static void assertSkip(Result result) {
        assertEquals(TestResult.skip, result.getResult(), "expected result: SKIP");
    }

    /// Asserts that the given result has a test outcome of PASS.
    ///
    /// @param result the test result to check.
    public static void assertPass(Result result) {
        assertEquals(TestResult.pass, result.getResult(), "expected result: PASS");
    }

    /// Asserts that the given result has a test outcome of INFO.
    ///
    /// @param result the test result to check.
    public static void assertInfo(Result result) {
        assertEquals(TestResult.info, result.getResult(), "expected result: INFO");
    }

    /// Asserts that the given result has a test outcome of WARN.
    ///
    /// @param result the test result to check.
    public static void assertWarn(Result result) {
        assertEquals(TestResult.warn, result.getResult(), "expected result: WARN");
    }

    /// Asserts that the given result has a test outcome of FAIL with exactly one failure event.
    ///
    /// @param result the test result to check.
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

    /// Asserts that the given result has a test outcome of ERROR.
    ///
    /// @param result the test result to check.
    public static void assertError(Result result) {
        assertEquals(TestResult.error, result.getResult(), "expected result: ERROR");
    }
}
