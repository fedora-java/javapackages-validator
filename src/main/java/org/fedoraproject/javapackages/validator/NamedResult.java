package org.fedoraproject.javapackages.validator;

import java.time.Instant;
import java.util.Iterator;

import org.fedoraproject.javapackages.validator.spi.LogEntry;
import org.fedoraproject.javapackages.validator.spi.Result;
import org.fedoraproject.javapackages.validator.spi.TestResult;

class NamedResult implements Result {
    private Result delegate;
    private String testName;
    private Instant startTime;
    private Instant endTime;

    NamedResult(Result delegate, String testName, Instant startTime, Instant endTime) {
        this.delegate = delegate;
        this.testName = testName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    NamedResult(Result delegate, String testName) {
        this(delegate, testName, null, null);
    }

    @Override
    public Iterator<LogEntry> iterator() {
        return delegate.iterator();
    }

    @Override
    public TestResult getResult() {
        return delegate.getResult();
    }

    public String getTestName() {
        return testName;
    }

    Instant getStartTime() {
        return startTime;
    }

    Instant getEndTime() {
        return endTime;
    }
}
