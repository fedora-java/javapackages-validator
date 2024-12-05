package org.fedoraproject.javapackages.validator.util;

import java.util.List;

import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.LogEntry;
import org.fedoraproject.javapackages.validator.spi.Result;
import org.fedoraproject.javapackages.validator.spi.ResultBuilder;
import org.fedoraproject.javapackages.validator.spi.TestResult;

public class ConcurrentResultBuilder extends ResultBuilder {
    public ConcurrentResultBuilder() {
        super();
    }
    public synchronized Result build() {
        return super.build();
    }

    public synchronized TestResult getResult() {
        return super.getResult();
    }

    public synchronized List<LogEntry> getLog() {
        return super.getLog();
    }

    public synchronized void setResult(TestResult result) {
        super.setResult(result);
    }

    public synchronized void mergeResult(TestResult result) {
        super.mergeResult(result);
    }

    public synchronized void addLog(LogEntry logEntry) {
        super.addLog(logEntry);
    }

    public synchronized void debug(String pattern, Decorated... objects) {
        super.debug(pattern, objects);
    }

    public synchronized void skip(String pattern, Decorated... objects) {
        super.skip(pattern, objects);
    }

    public synchronized void pass(String pattern, Decorated... objects) {
        super.pass(pattern, objects);
    }

    public synchronized void info(String pattern, Decorated... objects) {
        super.info(pattern, objects);
    }

    public synchronized void warn(String pattern, Decorated... objects) {
        super.warn(pattern, objects);
    }

    public synchronized void fail(String pattern, Decorated... objects) {
        super.fail(pattern, objects);
    }

    public synchronized void error(String pattern, Decorated... objects) {
        super.error(pattern, objects);
    }

    public synchronized void error(Throwable ex) {
        super.error(ex);
    }
}
