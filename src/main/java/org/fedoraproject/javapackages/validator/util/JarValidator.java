package org.fedoraproject.javapackages.validator.util;

import java.util.function.Predicate;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.fedoraproject.javapackages.validator.spi.Decoration;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public abstract class JarValidator extends ElementwiseValidator implements RpmJarConsumer {
    public static final Decoration DECORATION_JAR = new Decoration(Decoration.Color.blue, Decoration.Modifier.bright);

    protected JarValidator() {
        super(Predicate.not(RpmInfo::isSourcePackage));
    }

    protected JarValidator(Predicate<RpmInfo> filter) {
        super(filter);
    }

    @Override
    public void validate(RpmPackage rpm) throws Exception {
        accept(rpm);
    }

    protected abstract void acceptJarEntryExcept(RpmPackage rpm, CpioArchiveEntry rpmEntry, byte[] content) throws Exception;

    @Override
    public void acceptJarEntry(RpmPackage rpm, CpioArchiveEntry rpmEntry, byte[] content) {
        try {
            acceptJarEntryExcept(rpm, rpmEntry, content);
        } catch (Exception ex) {
            error(ex);
        }
    }
}
