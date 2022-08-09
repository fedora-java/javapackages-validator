package org.fedoraproject.javapackages.validator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.EnumMap;

import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;

public class Logger {
    public enum LogEvent {
        debug("DEBUG"),
        info("INFO"),
        warn("WARN"),
        pass("PASS"),
        ;

        public final String text;

        private LogEvent(String text) {
            this.text = text;
        }
    }

    private EnumMap<LogEvent, PrintStream> streams = new EnumMap<>(LogEvent.class);

    public Logger() {
        setStream(LogEvent.debug, Main.getDebugOutputStream());
        setStream(LogEvent.info, System.err);
        setStream(LogEvent.warn, System.err);
        setStream(LogEvent.pass, new PrintStream(OutputStream.nullOutputStream(), false, StandardCharsets.UTF_8));
    }

    public void setStream(LogEvent logEvent, PrintStream stream) {
        streams.put(logEvent, stream);
    }

    private void log(LogEvent logEvent, String pattern, Object... arguments) {
        var stream = streams.get(logEvent);
        stream.print('[');
        Decoration[] decorations;
        switch (logEvent) {
        case debug:
            decorations = new Decoration[] {Decoration.bright_magenta, Decoration.bold};
            break;
        case info:
            decorations = new Decoration[] {Decoration.cyan, Decoration.bold};
            break;
        case warn:
            decorations = new Decoration[] {Decoration.yellow, Decoration.bold};
            break;
        case pass:
            decorations = new Decoration[] {Decoration.green, Decoration.bold};
            break;
        default:
            decorations = new Decoration[0];
            break;
        }
        stream.print(Main.getDecorator().decorate(logEvent.text, decorations));
        stream.print("] ");
        stream.println(MessageFormat.format(pattern, arguments));
    }

    public void debug(String pattern, Object... arguments) {
        log(LogEvent.debug, pattern, arguments);
    }

    public void info(String pattern, Object... arguments) {
        log(LogEvent.info, pattern, arguments);
    }

    public void warn(String pattern, Object... arguments) {
        log(LogEvent.warn, pattern, arguments);
    }

    public void pass(String pattern, Object... arguments) {
        log(LogEvent.pass, pattern, arguments);
    }
}
