package org.fedoraproject.javapackages.validator.validators;

import static org.fedoraproject.javapackages.validator.TestCommon.assertFailOne;
import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;

import java.nio.file.Paths;

import org.easymock.EasyMock;
import org.fedoraproject.javapackages.validator.TestCommon;
import org.fedoraproject.javapackages.validator.util.RpmFilesizeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.kojan.javadeptools.rpm.RpmPackage;

public class RpmFilesizeValidatorTest {
    private RpmPackage rpm;
    private RpmFilesizeValidator validator;

    @BeforeEach
    public void setUp() throws Exception {
        rpm = new RpmPackage(
                TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/duplicate-file1-1-1.noarch.rpm")));
        validator = EasyMock.createStrictMock(RpmFilesizeValidator.class);
    }

    private void runTest() throws Exception {
        EasyMock.replay(validator);
        validator.validate(rpm);
        EasyMock.verify(validator);
    }

    @Test
    @Disabled
    public void testAllowedFileSize() throws Exception {
        EasyMock.expect(validator.allowedFilesize(rpm.getInfo(), 6488L)).andReturn(true);
        runTest();
        assertPass(validator.build());
    }

    @Test
    @Disabled
    public void testDisallowedFileSize() throws Exception {
        EasyMock.expect(validator.allowedFilesize(rpm.getInfo(), 6488L)).andReturn(false);
        runTest();
        assertFailOne(validator.build());
    }
}
