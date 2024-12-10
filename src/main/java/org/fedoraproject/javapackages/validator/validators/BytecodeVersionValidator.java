package org.fedoraproject.javapackages.validator.validators;

import java.nio.file.Path;
import java.util.Map;

import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.TestResult;
import org.fedoraproject.javapackages.validator.util.BytecodeVersionJarValidator;

import io.kojan.javadeptools.rpm.RpmPackage;

/**
 * BytecodeVersionValidator can be ran in two modes &ndash; enforcing and
 * informative. When configured with max and min bytecode versions (the limits)
 * then it enforces class bytecode versions, producing failures for cases where
 * the actual version does not fit within the configured range. Otherwise, when
 * ran without limits specified, its results are only informative &ndash; it
 * only prints bytecode versions found as infos.
 */
public class BytecodeVersionValidator extends BytecodeVersionJarValidator {
    @Override
    public String getTestName() {
        return "/java/bytecode-version";
    }

    private static record Limits(short min, short max) {
    }

    public Limits readLimits() {
        var args = getArgs();
        if (args == null) {
            return null;
        }
        if (args.size() != 1) {
            throw new IllegalArgumentException("Wrong number of arguments, expected 1");
        }

        var arg = args.getFirst();
        var pos = arg.indexOf(':');

        short min = Short.MIN_VALUE;
        short max = Short.MAX_VALUE;

        if (pos == -1) {
            var num = Short.parseShort(arg);
            min = num;
            max = num;
        } else {
            if (pos != 0) {
                min = Short.parseShort(arg.substring(0, pos));
            }
            if (pos != arg.length() - 1) {
                max = Short.parseShort(arg.substring(pos + 1));
            }

            if (min > max) {
                throw new IllegalArgumentException("Specified minimum is larger than specified maximum");
            }
        }

        return new Limits(min, max);
    }

    @Override
    public void validate(RpmPackage rpm, Path jarPath, Map<Path, Version> classVersions) {
        var limits = readLimits();
        if (limits == null) {
            super.validate(rpm, jarPath, classVersions);
            return;
        }

        debug("Limits: {0}:{1}", Decorated.plain(limits.min()), Decorated.plain(limits.max()));
        for (var entry : classVersions.entrySet()) {
            boolean failed = false;

            if (entry.getValue().major() < limits.min()) {
                failed = true;
                fail("{0}: {1}: {2}: bytecode version: {3} is smaller than {4}",
                        Decorated.rpm(rpm),
                        Decorated.custom(jarPath, DECORATION_JAR),
                        Decorated.struct(entry.getKey()),
                        Decorated.actual(entry.getValue()),
                        Decorated.expected(limits.min()));
            }

            if (limits.max() < entry.getValue().major()) {
                failed = true;
                fail("{0}: {1}: {2}: bytecode version: {3} is larger than {4}",
                        Decorated.rpm(rpm),
                        Decorated.custom(jarPath, DECORATION_JAR),
                        Decorated.struct(entry.getKey()),
                        Decorated.actual(entry.getValue()),
                        Decorated.expected(limits.max()));
            }

            if (!failed) {
                mergeResult(TestResult.pass);
                debug("{0}: {1}: {2}: bytecode version: {3}",
                        Decorated.rpm(rpm),
                        Decorated.custom(jarPath, DECORATION_JAR),
                        Decorated.struct(entry.getKey()),
                        Decorated.actual(entry.getValue()));
            }
        }
    }
}
