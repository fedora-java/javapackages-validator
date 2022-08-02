package org.fedoraproject.javapackages.validator.config;

import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public interface FilesConfig {
    public static final class ExpectedProperties {
        private Long uid = null;
        private Long gid = null;
        private Long maxSize = null;
        private FileType fileType = null;
        private Set<PosixFilePermission> permissions = null;

        public static enum FileType {
            REGULAR, DIRECTORY, SYMLINK,
        }

        ExpectedProperties uid(long value) {
            uid = value;
            return this;
        }

        ExpectedProperties gid(long value) {
            gid = value;
            return this;
        }

        ExpectedProperties maxSize(long value) {
            maxSize = value;
            return this;
        }

        ExpectedProperties fileType(FileType value) {
            fileType = value;
            return this;
        }

        ExpectedProperties permissions(Set<PosixFilePermission> value) {
            permissions = value;
            return this;
        }

        public Long getUid() {
            return uid;
        }

        public Long getGid() {
            return gid;
        }

        public Long getMaxSize() {
            return maxSize;
        }

        public FileType getFileType() {
            return fileType;
        }

        public Set<PosixFilePermission> getPermissions() {
            if (permissions == null) {
                return null;
            }
            return Collections.unmodifiableSet(permissions);
        }
    }

    /**
     * @deprecated Use fileProperties
     */
    @Deprecated
    boolean allowedFile(RpmPackage rpm, Path path);

    /**
     * @param rpm RpmPackage.
     * @param path The absolute path of a file present in the RPM.
     * @return null if the file is illegal. Otherwise the expected properties of
     * the file. Each field may be null, which means that that property is not
     * checked.
     */
    default ExpectedProperties fileProperties(RpmPackage rpm, Path path) {
        return allowedFile(rpm, path) ? new ExpectedProperties() : null;
    }

    /**
     * An additional check for missing files in the RPM.
     * @param rpm RpmPackage.
     * @param paths The set of paths present in the RPM.
     * @return A collection of files that were expected to be present but were not found.
     */
    default Collection<Path> missingFiles(RpmPackage rpm, Set<Path> paths) {
        return Collections.emptyList();
    }
}
