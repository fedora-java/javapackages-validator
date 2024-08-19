package org.fedoraproject.javapackages.validator;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.stream.Stream;

import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.LogEvent;

class Logger {
    private EnumMap<LogEvent, PrintStream> streams = new EnumMap<>(LogEvent.class);

    public Logger() {
        setStream(LogEvent.debug, Main.getDebugOutputStream());
    }

    public void setStream(LogEvent logEvent, PrintStream stream) {
        streams.put(logEvent, stream);
    }

    private void log(LogEvent logEvent, String pattern, Decorated... arguments) {
        streams.get(logEvent).append("[" + Main.decorate(logEvent.getDecorated()) + "] ")
        .println(MessageFormat.format(pattern, Stream.of(arguments)
                .map(Main::decorate).toArray()));
    }

    public void debug(String pattern, Decorated... arguments) {
        log(LogEvent.debug, pattern, arguments);
    }
}
