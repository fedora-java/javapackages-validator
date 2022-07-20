package org.fedoraproject.javapackages.validator.checks;

import org.fedoraproject.javapackages.validator.RpmAttributeCheck;
import org.fedoraproject.javapackages.validator.config.ConflictsConfig;

public class ConflictsCheck extends RpmAttributeCheck<ConflictsConfig> {
    public ConflictsCheck() {
        this(null);
    }

    public ConflictsCheck(ConflictsConfig config) {
        super(config);
    }

    public static void main(String[] args) throws Exception {
        System.exit(new ConflictsCheck().executeCheck(ConflictsConfig.class, args));
    }
}
