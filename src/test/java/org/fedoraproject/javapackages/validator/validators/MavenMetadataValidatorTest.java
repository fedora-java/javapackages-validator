package org.fedoraproject.javapackages.validator.validators;

import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;

import java.nio.file.Path;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.junit.jupiter.api.Test;

public class MavenMetadataValidatorTest {
    private static final Path MD_RPM = TestCommon.RPM_PATH_PREFIX.resolve(Path.of("maven-metadata-1-1.noarch.rpm"));

    @Test
    public void testMavenMetadata() throws Exception {
        var validator = new MavenMetadataValidator();
        validator.validate(TestCommon.fromPaths(MD_RPM));
        assertPass(validator.build());
    }
}
