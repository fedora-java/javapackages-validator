package org.fedoraproject.javapackages.validator.util;

import java.util.function.Consumer;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.io.IOUtils;

import io.kojan.javadeptools.rpm.RpmArchiveInputStream;
import io.kojan.javadeptools.rpm.RpmPackage;

public interface RpmJarConsumer extends Consumer<RpmPackage> {
    @Override
    default void accept(RpmPackage rpm) {
        try (var is = new RpmArchiveInputStream(rpm.getPath())) {
            for (CpioArchiveEntry rpmEntry; (rpmEntry = is.getNextEntry()) != null;) {
                if (!rpmEntry.isSymbolicLink() && rpmEntry.getName().endsWith(".jar")) {
                    acceptJarEntry(rpm, rpmEntry, IOUtils.toByteArray(is));
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    void acceptJarEntry(RpmPackage rpm, CpioArchiveEntry rpmEntry, byte[] content) throws Exception;
}
