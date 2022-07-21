package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.fedoraproject.javapackages.validator.Check;
import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.RpmPathInfo;

public class JavadocNoarchCheck extends ElementwiseCheck<Check.NoConfig> {
    public JavadocNoarchCheck() {
        this(null);
    }

    public JavadocNoarchCheck(NoConfig config) {
        super(config);
        setFilter((rpm) -> {
            if (rpm.getInfo().isSourcePackage()) {
                return false;
            }

            String rpmName = rpm.getInfo().getName();

            if (rpmName.endsWith("-javadocs")) {
                rpmName = rpmName.substring(0, rpmName.length() - 1);
            }

            return rpmName.equals(Common.getPackageName(rpm.getInfo().getSourceRPM()) + "-javadoc");
        });
    }

    @Override
    public Collection<String> check(RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        if (!"noarch".equals(rpm.getInfo().getArch())) {
            result.add(MessageFormat.format(
                    "[FAIL] {0} is a javadoc package but its architecture is not noarch", rpm.getPath()));
        } else {
            System.err.println(MessageFormat.format(
                    "[INFO] {0} is a javadoc package and its architecture is noarch", rpm.getPath()));
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new JavadocNoarchCheck().executeCheck(Check.NoConfig.class, args));
    }
}
