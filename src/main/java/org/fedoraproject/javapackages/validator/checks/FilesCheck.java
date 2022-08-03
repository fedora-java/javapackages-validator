package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.fedoraproject.javadeptools.rpm.RpmArchiveInputStream;
import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.config.FilesConfig;
import org.fedoraproject.javapackages.validator.config.FilesConfig.ExpectedProperties;

public class FilesCheck extends ElementwiseCheck<FilesConfig> {
    public FilesCheck() {
        this(null);
    }

    public FilesCheck(FilesConfig config) {
        super(FilesConfig.class, config);
    }

    private static Set<PosixFilePermission> permissions(CpioArchiveEntry entry) {
        var result = new HashSet<PosixFilePermission>();

        if ((entry.getMode() & CpioConstants.C_IRUSR) != 0) result.add(PosixFilePermission.OWNER_READ);
        if ((entry.getMode() & CpioConstants.C_IWUSR) != 0) result.add(PosixFilePermission.OWNER_WRITE);
        if ((entry.getMode() & CpioConstants.C_IXUSR) != 0) result.add(PosixFilePermission.OWNER_EXECUTE);

        if ((entry.getMode() & CpioConstants.C_IRGRP) != 0) result.add(PosixFilePermission.GROUP_READ);
        if ((entry.getMode() & CpioConstants.C_IWGRP) != 0) result.add(PosixFilePermission.GROUP_WRITE);
        if ((entry.getMode() & CpioConstants.C_IXGRP) != 0) result.add(PosixFilePermission.GROUP_EXECUTE);

        if ((entry.getMode() & CpioConstants.C_IROTH) != 0) result.add(PosixFilePermission.OTHERS_READ);
        if ((entry.getMode() & CpioConstants.C_IWOTH) != 0) result.add(PosixFilePermission.OTHERS_WRITE);
        if ((entry.getMode() & CpioConstants.C_IXOTH) != 0) result.add(PosixFilePermission.OTHERS_EXECUTE);

        return result;
    }

    @Override
    public Collection<String> check(RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);
        var entrySet = new HashSet<Path>();

        try (var is = new RpmArchiveInputStream(rpm.getPath())) {
            var previousSize = result.size();
            for (CpioArchiveEntry rpmEntry; ((rpmEntry = is.getNextEntry()) != null);) {
                Path entryName = Common.getEntryPath(rpmEntry);
                entrySet.add(entryName);
                ExpectedProperties fileProperties = getConfig().fileProperties(rpm.getRpmPackage(), entryName);
                if (fileProperties == null) {
                    result.add(MessageFormat.format("[FAIL] {0}: Illegal file: {1}",
                            rpm.getPath(), entryName));
                    continue;
                }

                if (fileProperties.getUid() != null && fileProperties.getUid() != rpmEntry.getUID()) {
                    result.add(MessageFormat.format("[FAIL] {0}: {1}: Illegal user ID: {2}, expected: {3}",
                            rpm.getPath(), entryName, rpmEntry.getUID(), fileProperties.getUid()));
                }
                if (fileProperties.getGid() != null && fileProperties.getGid() != rpmEntry.getGID()) {
                    result.add(MessageFormat.format("[FAIL] {0}: {1}: Illegal group ID: {2}, expected: {3}",
                            rpm.getPath(), entryName, rpmEntry.getGID(), fileProperties.getGid()));
                }
                if (fileProperties.getMaxSize() != null && fileProperties.getMaxSize() < rpmEntry.getSize()) {
                    result.add(MessageFormat.format("[FAIL] {0}: {1}: Illegal file size: {2} exceeds expected {3}",
                            rpm.getPath(), entryName, rpmEntry.getSize(), fileProperties.getMaxSize()));
                }
                if (fileProperties.getFileType() != null) {
                    switch (fileProperties.getFileType()) {
                    case REGULAR:
                        if (!rpmEntry.isRegularFile()) {
                            result.add(MessageFormat.format("[FAIL] {0}: {1}: Illegal file type, expected regular file",
                                    rpm.getPath(), entryName));
                        }
                        break;
                    case DIRECTORY:
                        if (!rpmEntry.isDirectory()) {
                            result.add(MessageFormat.format("[FAIL] {0}: {1}: Illegal file type, expected symbolic link",
                                    rpm.getPath(), entryName));
                        }
                        break;
                    case SYMLINK:
                        if (!rpmEntry.isSymbolicLink()) {
                            result.add(MessageFormat.format("[FAIL] {0}: {1}: Illegal file type, expected directory",
                                    rpm.getPath(), entryName));
                        }
                        break;
                    }
                }
                var expectedPermissions = fileProperties.getPermissions();
                if (expectedPermissions != null) {
                    var entryPermissions = permissions(rpmEntry);
                    if (!entryPermissions.equals(expectedPermissions)) {
                        result.add(MessageFormat.format("[FAIL] {0}: {1}: Illegal file permissions: {2}, expected: {3}",
                                rpm.getPath(), entryName, PosixFilePermissions.toString(entryPermissions),
                                PosixFilePermissions.toString(expectedPermissions)));
                    }
                }
            }

            if (previousSize == result.size()) {
                System.err.println(MessageFormat.format("[INFO] {0}: ok", rpm.getPath()));
            }
        }

        for (Path missingFile : getConfig().missingFiles(rpm.getRpmPackage(), entrySet)) {
            result.add(MessageFormat.format("[FAIL] {0}: Missing file: {1}",
                    rpm.getPath(), missingFile));
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new FilesCheck().executeCheck(args));
    }
}
