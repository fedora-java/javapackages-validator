package org.fedoraproject.javapackages.validator.config;

import java.util.List;

public interface ExclusiveArchConfig {
    boolean allowedExclusiveArch(RpmPackage rpm, List<String> values);
}
