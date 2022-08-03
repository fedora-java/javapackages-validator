package org.fedoraproject.javapackages.validator.checks;

import org.fedoraproject.javapackages.validator.RpmAttributeCheck;
import org.fedoraproject.javapackages.validator.config.RecommendsConfig;

public class RecommendsCheck extends RpmAttributeCheck<RecommendsConfig> {
    public RecommendsCheck() {
        this(null);
    }

    public RecommendsCheck(RecommendsConfig config) {
        super(RecommendsConfig.class, config);
    }

    public static void main(String[] args) throws Exception {
        System.exit(new RecommendsCheck().executeCheck(args));
    }
}
