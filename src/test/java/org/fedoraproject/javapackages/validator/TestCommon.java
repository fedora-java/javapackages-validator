package org.fedoraproject.javapackages.validator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

public class TestCommon {
    public static final Path RPMBUILD_PATH_PREFIX = Paths.get("src/test/resources/rpmbuild");
    public static final Path RPM_PATH_PREFIX = RPMBUILD_PATH_PREFIX.resolve(Paths.get("RPMS"));
    public static final Path SRPM_PATH_PREFIX = RPMBUILD_PATH_PREFIX.resolve(Paths.get("SRPMS"));

    public static Iterator<? extends RpmPathInfo> iteratorFrom(Stream<Path> paths) {
        return paths.map(RpmPackageInfo::new).iterator();
    }
}
