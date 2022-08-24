package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.fedoraproject.javapackages.validator.Check;
import org.fedoraproject.javapackages.validator.Decorated;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.RpmPathInfo;

public class JavadocNoarchCheck extends ElementwiseCheck<Check.NoConfig> {
    public JavadocNoarchCheck() {
        super(Check.NoConfig.class);
        setFilter(rpm -> !rpm.isSourcePackage() && rpm.getName().equals(rpm.getPackageName() + "-javadoc"));
    }

    @Override
    public Collection<String> check(Check.NoConfig config, RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        if (!"noarch".equals(rpm.getArch())) {
            result.add(failMessage("{0} is a javadoc package but its architecture is {1}",
                    Decorated.rpm(rpm.getPath()),
                    Decorated.actual(rpm.getArch())));
        } else {
            getLogger().pass("{0} is a javadoc package and its architecture is {1}",
                    Decorated.rpm(rpm.getPath()),
                    Decorated.actual("noarch"));
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new JavadocNoarchCheck().executeCheck(args));
    }
}
