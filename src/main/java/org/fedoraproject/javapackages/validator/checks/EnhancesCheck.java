package org.fedoraproject.javapackages.validator.checks;

import org.fedoraproject.javapackages.validator.RpmAttributeCheck;
import org.fedoraproject.javapackages.validator.config.EnhancesConfig;

public class EnhancesCheck extends RpmAttributeCheck<EnhancesConfig> {
    public EnhancesCheck() {
        this(null);
    }

    public EnhancesCheck(EnhancesConfig config) {
        super(config);
    }

    public static void main(String[] args) throws Exception {
        System.exit(new EnhancesCheck().executeCheck(EnhancesConfig.class, args));
    }
}
