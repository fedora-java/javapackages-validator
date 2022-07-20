package org.fedoraproject.javapackages.validator.config;

import java.util.List;

public interface RpmPackage extends Nevra {
    String getPackageName();
    boolean isSourceRpm();
    List<String> getBuildArchs();
}
