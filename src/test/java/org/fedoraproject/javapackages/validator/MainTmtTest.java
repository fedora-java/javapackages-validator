package org.fedoraproject.javapackages.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.fedoraproject.javapackages.validator.spi.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

    void copyResource(String loc) throws Exception {
        Path path = Paths.get("src/test/resources").resolve(loc);
        Path dest = artifactsDir.resolve(path.getFileName());
        Files.copy(path, dest);
    }

    void addValidator(String name, AnonymousValidator validator) {
        if (TestFactory.validators.isEmpty()) {
            args.add(TestFactory.class.getCanonicalName());
        }
        TestFactory.validators.add(new TestValidator(name, validator));
    }

    void runMain(int expRc) throws Exception {
        int rc=main.run(args.toArray(new String[args.size()]));
        assertEquals(expRc, rc,"check expected return code");
    }

    void expectResults(String... locations) {
        for (String loc : locations) {
            Path path = tmtTestData.resolve(loc);
            assertTrue(Files.isRegularFile(path), "Result " + path + "is present");
        }
    }

    String readResult(String loc) throws Exception {
        expectResults(loc);
        Path path = tmtTestData.resolve(loc);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    @Test
    void testCrashLog() throws Exception {

        // Corrupted empty file triggers the crash
        Files.createFile(artifactsDir.resolve("empty.rpm"));

        runMain(2);
        expectResults("results.yaml", "crash.log");

        assertTrue(readResult("results.yaml").contains("result: error"), "result is error");
        assertTrue(readResult("crash.log").contains("java.io.IOException: Unable to open RPM file"),
                "crash.log contains stack trace");
    }

    @Test
    @Disabled("https://github.com/fedora-java/javapackages-validator/issues/82")
    void testHtmlNewLine() throws Exception {
        copyResource("arg_file_iterator/dangling-symlink-1-1.noarch.rpm");

        addValidator("/html-new-line", (rpms, rb) -> {
            rb.addLog(LogEntry.warn("first_line\nsecond_line"));
        });

        runMain(0);
        expectResults( //
                "results/html-new-line.html", //
                "results/html-new-line.log", //
                "results.yaml");

        assertTrue(readResult("results/html-new-line.html") //
                .contains("first_line<br>second_line"), //
                "new line is represented as <br>");
        assertTrue(readResult("results.yaml").contains("result: warn"), "result is warn");
    }

}
