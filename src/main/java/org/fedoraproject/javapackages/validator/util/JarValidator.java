package org.fedoraproject.javapackages.validator.util;

import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.spi.Decoration;
import org.fedoraproject.javapackages.validator.util.JarValidator.RpmJarResultBuilder;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public abstract class JarValidator extends ConcurrentValidator {
    public static final Decoration DECORATION_JAR = new Decoration(Decoration.Color.blue, Decoration.Modifier.bright);

    protected JarValidator() {
        super(Predicate.not(RpmInfo::isSourcePackage));
    }

    protected JarValidator(Predicate<RpmInfo> filter) {
        super(filter);
    }

    protected static abstract class RpmJarResultBuilder extends ElementwiseResultBuilder implements RpmJarConsumer {
        @Override
        public void validate(RpmPackage rpm) throws Exception {
            accept(rpm);
        }
    }

    @Override
    protected abstract RpmJarResultBuilder spawnValidator();
}
