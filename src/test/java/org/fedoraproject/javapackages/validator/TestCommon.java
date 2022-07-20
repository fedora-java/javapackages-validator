package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

import org.fedoraproject.javadeptools.rpm.RpmInfo;

public class TestCommon {
    public static final Path RPMBUILD_PATH_PREFIX = Paths.get("src/test/resources/rpmbuild");
    public static final Path RPM_PATH_PREFIX = RPMBUILD_PATH_PREFIX.resolve(Paths.get("RPMS"));
    public static final Path SRPM_PATH_PREFIX = RPMBUILD_PATH_PREFIX.resolve(Paths.get("SRPMS"));

    public static Iterator<RpmInfo> iteratorFrom(Stream<Path> paths) {
        return paths.map(path -> {
            try {
                return new RpmInfo(path);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }).iterator();
    }
}
