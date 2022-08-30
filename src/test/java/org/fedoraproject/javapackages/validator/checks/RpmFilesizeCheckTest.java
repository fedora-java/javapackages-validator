package org.fedoraproject.javapackages.validator.checks;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fedoraproject.javapackages.validator.TestCommon.assertFailOne;
import static org.fedoraproject.javapackages.validator.TestCommon.assertPass;

import java.nio.file.Paths;

import org.fedoraproject.javapackages.validator.CheckResult;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.TestCommon;
import org.fedoraproject.javapackages.validator.config.RpmFilesizeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RpmFilesizeCheckTest {
    private RpmPathInfo rpmPathInfo;
    private RpmFilesizeConfig conf;

    @BeforeEach
    public void setUp() throws Exception {
        rpmPathInfo = new RpmPathInfo(
                TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/duplicate-file1-1-1.noarch.rpm")));
        conf = createStrictMock(RpmFilesizeConfig.class);
    }

    private CheckResult runTest() throws Exception {
        replay(conf);
        var result = new RpmFilesizeCheck().check(conf, rpmPathInfo);
        verify(conf);
        return result;
    }

    @Test
    public void testAllowedFileSize() throws Exception {
        expect(conf.allowedFilesize(rpmPathInfo, 6488L)).andReturn(true);
        assertPass(runTest());
    }

    @Test
    public void testDisallowedFileSize() throws Exception {
        expect(conf.allowedFilesize(rpmPathInfo, 6488L)).andReturn(false);
        assertFailOne(runTest());
    }
}
