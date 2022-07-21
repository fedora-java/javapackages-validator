package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.config.ExclusiveArchConfig;

public class ExclusiveArchCheck extends ElementwiseCheck<ExclusiveArchConfig> {
    public ExclusiveArchCheck() {
        this(null);
    }

    public ExclusiveArchCheck(ExclusiveArchConfig config) {
        super(config);
        setFilter((rpm) -> rpm.getInfo().isSourcePackage());
    }

    @Override
    protected Collection<String> check(RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        if (!getConfig().allowedExclusiveArch(rpm.getRpmPackage(), rpm.getInfo().getExclusiveArch())) {
            result.add(MessageFormat.format("[FAIL] {0}: ExclusiveArch with values {1} failed",
                    rpm.getPath(), rpm.getInfo().getExclusiveArch()));
        } else {
            System.err.println(MessageFormat.format("[INFO] {0}: ExclusiveArch with values {1} passed",
                    rpm.getPath(), rpm.getInfo().getExclusiveArch()));
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new ExclusiveArchCheck().executeCheck(ExclusiveArchConfig.class, args));
    }
}
