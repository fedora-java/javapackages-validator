package org.fedoraproject.javapackages.validator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.EnumMap;

import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;

public class Logger {
    public enum LogEvent {
        debug("DEBUG", Decoration.bright_magenta, Decoration.bold),
        info("INFO", Decoration.cyan, Decoration.bold),
        pass("PASS", Decoration.green, Decoration.bold),
        ;

        private final Decorated decoratedText;

        private LogEvent(String text, Decoration...decorations) {
            this.decoratedText = Decorated.custom(text, decorations);
        }

        public Decorated getDecoratedText() {
            return decoratedText;
        }
    }

    private EnumMap<LogEvent, PrintStream> streams = new EnumMap<>(LogEvent.class);

    public Logger() {
        setStream(LogEvent.debug, Main.getDebugOutputStream());
        setStream(LogEvent.info, System.err);
        setStream(LogEvent.pass, new PrintStream(OutputStream.nullOutputStream(), false, StandardCharsets.UTF_8));
    }

    public void setStream(LogEvent logEvent, PrintStream stream) {
        streams.put(logEvent, stream);
    }

    private void log(LogEvent logEvent, String pattern, Decorated... arguments) {
        var stream = streams.get(logEvent);
        stream.print('[');
        stream.print(logEvent.getDecoratedText());
        stream.print("] ");
        stream.println(MessageFormat.format(pattern, arguments));
    }

    public void debug(String pattern, Decorated... arguments) {
        log(LogEvent.debug, pattern, arguments);
    }

    public void info(String pattern, Decorated... arguments) {
        log(LogEvent.info, pattern, arguments);
    }

    public void pass(String pattern, Decorated... arguments) {
        log(LogEvent.pass, pattern, arguments);
    }
}
