package org.fedoraproject.javapackages.validator;

import java.time.Instant;
import java.util.Iterator;

import org.fedoraproject.javapackages.validator.spi.LogEntry;
import org.fedoraproject.javapackages.validator.spi.Result;
import org.fedoraproject.javapackages.validator.spi.TestResult;

/// Represents a test result with an associated name and optional start and end
/// times. This class delegates result operations to another [Result]
/// instance.
class NamedResult implements Result {

    /// The delegate result instance.
    private Result delegate;
    /// The name of the test.
    private String testName;
    /// The start time of the test execution.
    private Instant startTime;
    /// The end time of the test execution.
    private Instant endTime;

    /// Constructs a [NamedResult] with a delegate result, test name, start
    /// time, and end time.
    ///
    /// @param delegate  the delegate [Result] instance
    /// @param testName  the name of the test
    /// @param startTime the start time of the test execution, or `null` if not
    ///                  available
    /// @param endTime   the end time of the test execution, or `null` if not
    ///                  available
    NamedResult(Result delegate, String testName, Instant startTime, Instant endTime) {
        this.delegate = delegate;
        this.testName = testName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /// Constructs a `NamedResult` with a delegate result and test name,
    /// without specifying start and end times.
    ///
    /// @param delegate the delegate `Result` instance
    /// @param testName the name of the test
    NamedResult(Result delegate, String testName) {
        this(delegate, testName, null, null);
    }

    /// Returns an iterator over the log entries of the result.
    ///
    /// @return an iterator over `LogEntry` objects
    @Override
    public Iterator<LogEntry> iterator() {
        return delegate.iterator();
    }

    /// Retrieves the overall test result.
    ///
    /// @return the `TestResult` of the delegate
    @Override
    public TestResult getResult() {
        return delegate.getResult();
    }

    /// Returns the name of the test.
    ///
    /// @return the test name
    public String getTestName() {
        return testName;
    }

    /// Returns the start time of the test execution, if available.
    ///
    /// @return the start time, or `null` if not set
    Instant getStartTime() {
        return startTime;
    }

    /// Returns the end time of the test execution, if available.
    ///
    /// @return the end time, or `null` if not set
    Instant getEndTime() {
        return endTime;
    }
}
