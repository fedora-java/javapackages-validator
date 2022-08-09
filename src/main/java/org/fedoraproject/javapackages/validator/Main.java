package org.fedoraproject.javapackages.validator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.fedoraproject.javapackages.validator.Logger.LogEvent;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class Main {
    private static TextDecorator DECORATOR = TextDecorator.NO_DECORATOR;
    private static PrintStream debugOutputStream = new PrintStream(OutputStream.nullOutputStream(), false, StandardCharsets.UTF_8);
    static Collection<RpmPackageInfo> TEST_RPMS;

    public static TextDecorator getDecorator() {
        return DECORATOR;
    }

    @SuppressFBWarnings({"MS_EXPOSE_REP"})
    public static PrintStream getDebugOutputStream() {
        return debugOutputStream;
    }

    public static Collection<? extends RpmPathInfo> getTestRpms() {
        return Collections.unmodifiableCollection(TEST_RPMS);
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("error: no arguments provided");
            System.out.println("Usage: Main <simple class name of the check> [optional flags] <RPM files or directories to test...>");
            System.out.println("Optional flags:");
            System.out.println("    --config-src [/mnt/config/src] - directory containing configuration sources");
            System.out.println("    --config-bin [/mnt/config/bin] - directory where compiled class files will be put");
            System.out.println("    -x, --debug - Display debugging output");
            System.out.println("    -r, --color - Display colored output");
            System.exit(1);
        }

        var argList = new ArrayList<>(args.length);
        for (String arg : args) {
            if (arg.equals("-r") || arg.equals("--color")) {
                DECORATOR = AnsiDecorator.INSTANCE;
                continue;
            } else if (arg.equals("-x") || arg.equals("--debug")) {
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
