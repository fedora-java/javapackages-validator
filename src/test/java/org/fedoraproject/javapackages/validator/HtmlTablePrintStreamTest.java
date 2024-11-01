package org.fedoraproject.javapackages.validator;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.fedoraproject.javapackages.validator.MainTmt.HtmlTablePrintStream;
import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.Decoration;
import org.fedoraproject.javapackages.validator.spi.Decoration.Color;
import org.fedoraproject.javapackages.validator.spi.Decoration.Modifier;
import org.fedoraproject.javapackages.validator.spi.LogEntry;
import org.fedoraproject.javapackages.validator.spi.LogEvent;
import org.fedoraproject.javapackages.validator.spi.TestResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HtmlTablePrintStreamTest {

    @TempDir
    Path outDir;

    @Test
    void testHtmlGeneration() throws Exception {
        var lorem =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do "
                        + "eiusmod tempor incididunt ut labore et dolore magna aliqua.";

        List<Optional<Color>> colors = new ArrayList<>();
        colors.add(Optional.empty());
        for (var color : Color.values()) {
            colors.add(Optional.of(color));
        }

        List<Modifier[]> modas = new ArrayList<>();
        for (int i = 0; i < 1 << Modifier.values().length; i++) {
            List<Modifier> modifiers = new ArrayList<>();
            for (int j = 0; j < Modifier.values().length; j++) {
                if ((i & (1 << j)) != 0) {
                    modifiers.add(Modifier.values()[j]);
                }
            }
            var moda = modifiers.toArray(new Modifier[modifiers.size()]);
            modas.add(moda);
        }

        List<LogEntry> entries = new ArrayList<>();
        for (LogEvent event : LogEvent.values()) {
            for (var moda : modas) {
                for (var color : colors) {
                    var entry =
                            new LogEntry(
                                    event,
                                    "Text in color {0} and modifiers {1}: {2}",
                                    Decorated.custom(color, new Decoration(color)),
                                    Decorated.custom(Arrays.asList(moda), new Decoration(moda)),
                                    Decorated.custom(lorem, new Decoration(color, moda)));
                    entries.add(entry);
                }
            }
        }

        Path htmlDir = outDir.resolve("html");
        Files.createDirectory(htmlDir);
        for (String res : List.of("filter.js", "style.css")) {
            try (var os = Files.newOutputStream(outDir.resolve(res));
                    var is = MainTmt.class.getResourceAsStream("/tmt_html/" + res)) {
                is.transferTo(os);
            }
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (HtmlTablePrintStream htps = new HtmlTablePrintStream(bos, TestResult.info)) {
                entries.forEach(htps::printRow);
            }
            Files.write(htmlDir.resolve("index.html"), bos.toByteArray());
        }
        System.err.println("Generated HTML is available at " + htmlDir);
    }
}
