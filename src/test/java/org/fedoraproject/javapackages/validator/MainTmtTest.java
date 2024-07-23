package org.fedoraproject.javapackages.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.fedoraproject.javapackages.validator.spi.LogEntry;
import org.fedoraproject.javapackages.validator.spi.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTmtTest {

    @TempDir
    Path tmtTree;
    @TempDir
    Path tmtTestData;
    @TempDir
    Path artifactsDir;

    Main main;
    List<String> args;

    @BeforeEach
    void setUp() {
        main = MainTmt.create(tmtTestData, tmtTree);

        args = new ArrayList<>();
        args.add("-x");
        args.add("-f");
        args.add(artifactsDir.toString());

        TestFactory.validators.clear();
    }

    int runMain() throws Exception {
        return main.run(args.toArray(new String[args.size()]));
    }

    @Test
    void testCrashLog() throws Exception {

        // Corrupted empty file triggers the crash
        Files.createFile(artifactsDir.resolve("empty.rpm"));

        int rc = runMain();

        assertEquals(2, rc, "Correct exit code");
        assertTrue(Files.isRegularFile(tmtTestData.resolve("results.yaml")), "results.yaml is present");
        assertTrue(Files.isRegularFile(tmtTestData.resolve("crash.log")), "crash.log is present");

        assertTrue(Files.readString(tmtTestData.resolve("results.yaml")).contains("result: error"), "result is error");
        assertTrue(Files.readString(tmtTestData.resolve("crash.log"))
                .contains("java.io.IOException: Unable to open RPM file"), "crash.log contains stack trace");
    }

    @Test
    void testHtmlNewLine() throws Exception {
        Files.copy(Paths.get("src/test/resources/arg_file_iterator/dangling-symlink-1-1.noarch.rpm"),
                artifactsDir.resolve("artifact.rpm"));
        Validator v = new TestValidator("/html-new-line", (rpms, rb) -> {
            rb.addLog(LogEntry.warn("first_line\nsecond_line"));
        });
        TestFactory.validators.add(v);
        args.add(TestFactory.class.getCanonicalName());

        runMain();

        assertTrue(Files.isRegularFile(tmtTestData.resolve("results/html-new-line.html")),
                "html-new-line.html is present");
        assertTrue(Files.readString(tmtTestData.resolve("results/html-new-line.html"))
                .contains("first_line<br>second_line"), "new line is represented as <br>");
    }

}
