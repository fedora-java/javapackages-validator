package org.fedoraproject.javapackages.validator;

import java.nio.file.Path;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.config.RpmPackage;

public interface RpmPathInfo {
    Path getPath();
    RpmInfo getInfo();
    RpmPackage getRpmPackage();
}
