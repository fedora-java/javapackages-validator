package org.fedoraproject.javapackages.validator.checks;

import static org.fedoraproject.javapackages.validator.TestCommon.assertFailOne;
import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.junit.jupiter.api.Test;

public class JavadocNoarchCheckTest {
    private static final Path ARCH_ARCH_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("x86_64/javadoc-arch-arch-javadoc-1-1.x86_64.rpm"));
    private static final Path ARCH_NOARCH_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/javadoc-arch-noarch-javadoc-1-1.noarch.rpm"));
    private static final Path NOARCH_NOARCH_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/javadoc-noarch-noarch-javadoc-1-1.noarch.rpm"));
    private static final Path NON_JAVADOC_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/javadoc-noarch-noarch-1-1.noarch.rpm"));

    @Test
    void testIllegalArchfulJavadoc() throws IOException {
        var result = new JavadocNoarchCheck().check(null,
                TestCommon.iteratorFrom(Stream.of(ARCH_ARCH_RPM)));
        assertFailOne(result);
    }

    @Test
    void testAllowedNoarchJavadocArchfulPackage() throws IOException {
        var result = new JavadocNoarchCheck().check(null,
                TestCommon.iteratorFrom(Stream.of(ARCH_NOARCH_RPM)));
        assertPass(result);
    }

    @Test
    void testAllowedNoarchJavadocNoarchPackage() throws IOException {
        var result = new JavadocNoarchCheck().check(null,
                TestCommon.iteratorFrom(Stream.of(NOARCH_NOARCH_RPM)));
        assertPass(result);
    }

    @Test
    void testIgnoreNonJavadoc() throws IOException {
        var result = new JavadocNoarchCheck().check(null,
                TestCommon.iteratorFrom(Stream.of(NON_JAVADOC_RPM)));
        assertPass(result);
    }
}
