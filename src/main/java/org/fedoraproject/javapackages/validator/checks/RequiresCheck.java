package org.fedoraproject.javapackages.validator.checks;

import org.fedoraproject.javapackages.validator.RpmAttributeCheck;
import org.fedoraproject.javapackages.validator.config.RequiresConfig;

public class RequiresCheck extends RpmAttributeCheck<RequiresConfig> {
    public RequiresCheck() {
        this(null);
    }

    public RequiresCheck(RequiresConfig config) {
        super(config);
    }

    public static void main(String[] args) throws Exception {
        System.exit(new RequiresCheck().executeCheck(RequiresConfig.class, args));
    }
}
