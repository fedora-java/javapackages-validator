package org.fedoraproject.javapackages.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.fedoraproject.javapackages.validator.Logger.LogEvent;

public class Main {
    private static TextDecorator DECORATOR = TextDecorator.NO_DECORATOR;
    static Collection<RpmPackageInfo> TEST_RPMS;

    public static TextDecorator getDecorator() {
        return DECORATOR;
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
            System.out.println("    -r, --color - Display colored output");
            System.exit(1);
        }

        var argList = new ArrayList<>(args.length);
        for (String arg : args) {
            if (arg.equals("-r") || arg.equals("--color")) {
                DECORATOR = AnsiDecorator.INSTANCE;
                continue;
            }

            argList.add(arg);
        }

        Check<?> check = Check.class.cast(Class.forName("org.fedoraproject.javapackages.validator.checks." + argList.get(0)).getConstructor().newInstance());
        check.getLogger().setStream(LogEvent.pass, System.out);
        System.exit(check.executeCheck(Arrays.copyOfRange(argList.toArray(String[]::new), 1, argList.size())));
    }
}
