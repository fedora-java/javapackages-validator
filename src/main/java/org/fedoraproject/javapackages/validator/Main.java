package org.fedoraproject.javapackages.validator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fedoraproject.javapackages.validator.Logger.LogEvent;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class Main {
    private static TextDecorator DECORATOR = TextDecorator.NO_DECORATOR;
    private static PrintStream debugOutputStream = new PrintStream(OutputStream.nullOutputStream(), false, StandardCharsets.UTF_8);

    public static TextDecorator getDecorator() {
        return DECORATOR;
    }

    @SuppressFBWarnings({"MS_EXPOSE_REP"})
    public static PrintStream getDebugOutputStream() {
        return debugOutputStream;
    }

    static record Flag(String... options) {
        static final Flag HELP = new Flag("-h", "--help");
        static final Flag CONFIG_FILE = new Flag("-c", "--config-file");
        static final Flag CONFIG_URI = new Flag("-u", "--config-uri");
        static final Flag COLOR = new Flag("-r", "--color");
        static final Flag DEBUG = new Flag("-x", "--debug");

        public boolean equals(String arg) {
            return Stream.of(options()).anyMatch(arg::equals);
        }

        @Override
        public String toString() {
            return Stream.of(options()).collect(Collectors.joining(", "));
        }
    }

    static void printHelp() {
        System.out.println("Usage: Main <simple class name of the check> [optional flags] <RPM files or directories to test...>");
        System.out.println("Optional flags:");
        System.out.println("    " + Flag.HELP.toString() + " - Print help message");
        System.out.println("    " + Flag.CONFIG_FILE.toString() + " - File path of a configuration source, can be specified multiple times");
        System.out.println("    " + Flag.CONFIG_URI.toString() + " - URI of a configuration source, can be specified multiple times");
        System.out.println("    " + Flag.DEBUG.toString() + " - Display debugging output");
        System.out.println("    " + Flag.COLOR.toString() + " - Display colored output");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("error: no arguments provided");
            printHelp();
            System.exit(1);
        } else if (Flag.HELP.equals(args[0])) {
            printHelp();
            System.exit(0);
        }

        var argList = new ArrayList<>(args.length);

        for (String arg : args) {
            if (Flag.COLOR.equals(arg)) {
                DECORATOR = AnsiDecorator.INSTANCE;
                continue;
            } else if (Flag.DEBUG.equals(arg)) {
                debugOutputStream = System.err;
                continue;
            }

            argList.add(arg);
        }

        Check<?> check = Check.class.cast(Class.forName("org.fedoraproject.javapackages.validator.checks." + argList.get(0)).getConstructor().newInstance());
        check.getLogger().setStream(LogEvent.pass, System.out);
        System.exit(check.executeCheck(Arrays.copyOfRange(argList.toArray(String[]::new), 1, argList.size())));
    }
}
