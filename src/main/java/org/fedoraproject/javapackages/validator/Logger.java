package org.fedoraproject.javapackages.validator;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.stream.Stream;

import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.LogEvent;

/// A simple logger that maps log events to their respective output streams. It
/// supports logging messages with a formatted pattern and decorated arguments.
class Logger {

    /// A mapping of log events to their respective output streams.
    private EnumMap<LogEvent, PrintStream> streams = new EnumMap<>(LogEvent.class);

    /// Constructs a `Logger` instance with a default debug stream.
    public Logger() {
        setStream(LogEvent.debug, Main.getDebugOutputStream());
    }

    /// Assigns a specific output stream to a given log event.
    ///
    /// @param logEvent the log event type
    /// @param stream   the output stream associated with the log event
    public void setStream(LogEvent logEvent, PrintStream stream) {
        streams.put(logEvent, stream);
    }

    /// Logs a message associated with a specific log event, formatting the message
    /// pattern with the provided decorated arguments.
    ///
    /// @param logEvent  the log event type
    /// @param pattern   the message format pattern
    /// @param arguments the decorated arguments for formatting the message
    private void log(LogEvent logEvent, String pattern, Decorated... arguments) {
        streams.get(logEvent).append("[" + Main.decorate(logEvent.getDecorated()) + "] ")
        .println(MessageFormat.format(pattern, Stream.of(arguments)
                .map(Main::decorate).toArray()));
    }

    /// Logs a debug message with a formatted pattern and decorated arguments.
    ///
    /// @param pattern   the message format pattern
    /// @param arguments the decorated arguments for formatting the message
    public void debug(String pattern, Decorated... arguments) {
        log(LogEvent.debug, pattern, arguments);
    }
}
