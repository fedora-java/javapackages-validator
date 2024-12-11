package org.fedoraproject.javapackages.validator.util;

import org.fedoraproject.javapackages.validator.spi.ResultBuilder;

import io.kojan.javadeptools.rpm.RpmPackage;

public abstract class ElementwiseResultBuilder extends ResultBuilder {
    public abstract void validate(RpmPackage rpm) throws Exception;
}
