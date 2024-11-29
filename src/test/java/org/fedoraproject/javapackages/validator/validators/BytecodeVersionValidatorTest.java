package org.fedoraproject.javapackages.validator.validators;

import static org.fedoraproject.javapackages.validator.TestCommon.assertError;
import static org.fedoraproject.javapackages.validator.TestCommon.assertFailOne;
import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;
import static org.fedoraproject.javapackages.validator.TestCommon.assertWarn;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.kojan.javadeptools.rpm.RpmPackage;

public class BytecodeVersionValidatorTest {
    static final Iterable<RpmPackage> RPMS = TestCommon.fromPaths(
            TestCommon.RPM_PATH_PREFIX.resolve(Path.of("noarch/jpms-automatic-1-1.noarch.rpm")),
            TestCommon.RPM_PATH_PREFIX.resolve(Path.of("noarch/jpms-invalid-1-1.noarch.rpm")));

    List<String> msgs;
    BytecodeVersionValidator validator;

    @BeforeEach
    void setUp() {
        msgs = new ArrayList<>();
        validator = new BytecodeVersionValidator() {
            public void addLog(LogEntry ent) {
                String msg = MessageFormat.format(ent.pattern(),
                        Stream.of(ent.objects()).map(Decorated::getObject).toArray());
                msgs.add(msg);
                System.out.println("[" + ent.kind().getDecorated().getObject() + "] " + msg);
                super.addLog(ent);
            }
        };
    }

    void assertMsg(String exp) {
        for (String msg : msgs) {
            if (msg.contains(exp)) {
                return;
            }
        }
        fail("Expected message: " + exp);
    }

    @Test
    void testInformative() throws Exception {
        validator.validate(RPMS, null);
        assertWarn(validator.build());
        assertMsg("module-info.class: bytecode version: 65.0");
        assertMsg("No limits were configured for /java/bytecode-version, the results will only be informative");
    }

    @Test
    void testPass() throws Exception {
        validator.validate(RPMS, List.of("12:345"));
        assertPass(validator.build());
        assertMsg("Limits: 12:345");
        assertMsg("found bytecode versions: [65.0]");
    }

    @Test
    void testFail() throws Exception {
        validator.validate(RPMS, List.of("42"));
        assertFailOne(validator.build());
        assertMsg("Limits: 42:42");
        assertMsg("bytecode version: 65.0 is larger than 42");
    }

    @Test
    void testEmptyArgs() throws Exception {
        validator.validate(RPMS, List.of());
        assertError(validator.build());
        assertMsg("Wrong number of arguments, expected 1");
    }

    @Test
    void testTooManyArgs() throws Exception {
        validator.validate(RPMS, List.of("12", "34"));
        assertError(validator.build());
        assertMsg("Wrong number of arguments, expected 1");
    }
}
