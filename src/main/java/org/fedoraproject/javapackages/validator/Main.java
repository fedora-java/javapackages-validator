package org.fedoraproject.javapackages.validator;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("error: no arguments provided");
            System.out.println("Usage: Main <simple class name of the check> [optional flags] <RPM files or directories to test...>");
            System.out.println("Optional flags:");
            System.out.println("    --config-src [/mnt/config/src] - directory containing configuration sources");
            System.out.println("    --config-bin [/mnt/config/bin] - directory where compiled class files will be put");
            System.exit(1);
        }

        Check<?> check = Check.class.cast(Class.forName("org.fedoraproject.javapackages.validator.checks." + args[0]).getConstructor().newInstance());
        System.exit(check.executeCheck(Arrays.copyOfRange(args, 1, args.length)));
    }
}
