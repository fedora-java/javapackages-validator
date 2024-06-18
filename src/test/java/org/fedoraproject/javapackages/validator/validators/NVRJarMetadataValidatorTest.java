package org.fedoraproject.javapackages.validator.validators;

import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.fedoraproject.javapackages.validator.TestCommon;
import org.junit.jupiter.api.Test;

public class NVRJarMetadataValidatorTest {
    private static final Path NO_EPOCH = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/nvr-metadata-no-epoch-1.2.abc~m2-1.el9.noarch.rpm"));
    private static final Path NO_EPOCH_SRC = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("nvr-metadata-no-epoch-1.2.abc~m2-1.el9.src.rpm"));
    private static final Path EPOCH_0 = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/nvr-metadata-epoch-0-1.2.abc~m2-1.el9.noarch.rpm"));
    private static final Path EPOCH_0_SRC = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("nvr-metadata-epoch-0-1.2.abc~m2-1.el9.src.rpm"));
    private static final Path EPOCH_1 = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/nvr-metadata-epoch-1-1.2.abc~m2-1.el9.noarch.rpm"));
    private static final Path EPOCH_1_SRC = TestCommon.SRPM_PATH_PREFIX.resolve(Paths.get("nvr-metadata-epoch-1-1.2.abc~m2-1.el9.src.rpm"));

    @Test
    public void testNVRMetadataNoEpoch() throws Exception {
        var validator = new NVRJarMetadataValidator();
        validator.validate(TestCommon.fromPaths(NO_EPOCH, NO_EPOCH_SRC));
        assertPass(validator.build());
    }

    @Test
    public void testNVRMetadataEpoch0() throws Exception {
        var validator = new NVRJarMetadataValidator();
        validator.validate(TestCommon.fromPaths(EPOCH_0, EPOCH_0_SRC));
        assertPass(validator.build());
    }

    @Test
    public void testNVRMetadataEpoch1() throws Exception {
        var validator = new NVRJarMetadataValidator();
        validator.validate(TestCommon.fromPaths(EPOCH_1, EPOCH_1_SRC));
        assertPass(validator.build());
    }
}
