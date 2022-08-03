package org.fedoraproject.javapackages.validator.checks;

import org.fedoraproject.javapackages.validator.RpmAttributeCheck;
import org.fedoraproject.javapackages.validator.config.SupplementsConfig;

public class SupplementsCheck extends RpmAttributeCheck<SupplementsConfig> {
    public SupplementsCheck() {
        this(null);
    }

    public SupplementsCheck(SupplementsConfig config) {
        super(SupplementsConfig.class, config);
    }

    public static void main(String[] args) throws Exception {
        System.exit(new SupplementsCheck().executeCheck(args));
    }
}
