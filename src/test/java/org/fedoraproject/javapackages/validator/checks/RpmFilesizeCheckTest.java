package org.fedoraproject.javapackages.validator.checks;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.CheckResult;
import org.fedoraproject.javapackages.validator.TestCommon;
import org.fedoraproject.javapackages.validator.config.RpmFilesizeConfig;
import org.junit.jupiter.api.Test;

public class RpmFilesizeCheckTest {

    Path rpmPath = TestCommon.RPM_PATH_PREFIX.resolve(Paths.get("noarch/duplicate-file1-1-1.noarch.rpm"));

    RpmFilesizeConfig conf = createStrictMock(RpmFilesizeConfig.class);

    private CheckResult runTest() throws Exception {
        replay(conf);
        var result = new RpmFilesizeCheck().check(conf, TestCommon.iteratorFrom(Stream.of(rpmPath)));
        verify(conf);
        return result;
    }

    @Test
    public void testAllowedFileSize() throws Exception {
        expect(conf.allowedFilesize(anyObject(RpmInfo.class), eq(6488L))).andReturn(true);
        assertTrue(runTest().isPass());
    }

    @Test
    public void testDisallowedFileSize() throws Exception {
        expect(conf.allowedFilesize(anyObject(RpmInfo.class), eq(6488L))).andReturn(false);
        var result = runTest();
        assertFalse(result.isPass());
        assertEquals(1, result.getFailureCount());
    }
}
