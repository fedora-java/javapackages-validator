package org.fedoraproject.javapackages.validator.util;

import java.util.function.Consumer;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.io.IOUtils;

import io.kojan.javadeptools.rpm.RpmArchiveInputStream;
import io.kojan.javadeptools.rpm.RpmPackage;

/**
 * Functional interface for processing JAR files within RPM packages.
 * <p>
 * This interface extends {@link Consumer} to allow for easy processing of RPM
 * packages and extracting JAR files contained within them.
 */
public interface RpmJarConsumer extends Consumer<RpmPackage> {

    /**
     * Processes an RPM package by scanning its contents and extracting JAR files.
     *
     * @param rpm The RPM package to process.
     * @throws RuntimeException If an exception occurs while reading the RPM
     *                          package.
     */
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

    /**
     * Handles an individual JAR file entry within an RPM package.
     *
     * @param rpm      The RPM package containing the JAR entry.
     * @param rpmEntry The archive entry representing the JAR file.
     * @param content  The byte content of the extracted JAR file.
     * @throws Exception If an error occurs while processing the JAR entry.
     */
    void acceptJarEntry(RpmPackage rpm, CpioArchiveEntry rpmEntry, byte[] content) throws Exception;
}
