package org.fedoraproject.javapackages.validator.util;

import java.util.ArrayList;
import java.util.concurrent.Future;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.fedoraproject.javapackages.validator.ThreadPool;

import io.kojan.javadeptools.rpm.RpmArchiveInputStream;
import io.kojan.javadeptools.rpm.RpmPackage;

public interface RpmJarConsumer {
    default void accept(RpmPackage rpm) throws Exception {
        var futures = new ArrayList<Future<?>>();
        try (var is = new RpmArchiveInputStream(rpm.getPath())) {
            for (CpioArchiveEntry nextRpmEntry; (nextRpmEntry = is.getNextEntry()) != null;) {
                if (!nextRpmEntry.isSymbolicLink() && nextRpmEntry.getName().endsWith(".jar")) {
                    var rpmEntry = nextRpmEntry;
                    var content = IOUtils.toByteArray(is);
                    futures.add(ThreadPool.submit(() -> acceptJarEntry(rpm, rpmEntry, content)));
                }
            }
            for (var future : futures) {
                future.get();
            }
        }
    }

    void acceptJarEntry(RpmPackage rpm, CpioArchiveEntry rpmEntry, byte[] content);
}
