package org.fedoraproject.javapackages.validator;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTmtTest {

    @TempDir
    Path tmtTree;
    @TempDir
    Path tmtTestData;
    @TempDir
    Path artifactsDir;

    @Test
    void testCrashLog() throws Exception {
        Main main = MainTmt.create(tmtTestData, tmtTree);

        List<String> args = new ArrayList<>();
        args.add("-x");
        args.add("-f");
        args.add(artifactsDir.toString());

        Files.createFile(artifactsDir.resolve("empty.rpm"));

        int rc = main.run(args.toArray(new String[args.size()]));

        assertEquals(2, rc, "Correct exit code");
        assertTrue(Files.isRegularFile(tmtTestData.resolve("results.yaml")), "results.yaml is present");
        assertTrue(Files.isRegularFile(tmtTestData.resolve("crash.log")), "crash.log is present");

        assertTrue(Files.readString(tmtTestData.resolve("results.yaml")).contains("result: error"), "result is error");
        assertTrue(Files.readString(tmtTestData.resolve("crash.log"))
                .contains("java.io.IOException: Unable to open RPM file"), "crash.log contains stack trace");
    }

}
