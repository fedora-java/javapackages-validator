package org.fedoraproject.javapackages.validator.config;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public interface SymlinkConfig {
    /**
     * @param target The link target. Is always absolute.
     * @return The textual representation of the location where the link target
     * was found. Can be a file on the file system or an rpm file. Returning
     * {@code null} means the target was not found.
     */
    String targetLocation(Path target);

    /**
     * Default implementation of {@link org.fedoraproject.javapackages.validator.config.SymlinkConfig}
     * which resolves symbolic link targets against files present on the file
     * system with specified environment root.
     */
    public static class EnvrootImpl implements SymlinkConfig {
        private Path envroot;

        public EnvrootImpl(Path envroot) {
            this.envroot = envroot;
        }

        @Override
        @SuppressFBWarnings({"DMI_HARDCODED_ABSOLUTE_FILENAME"})
        public String targetLocation(Path target) {
            Path result = envroot.resolve(Paths.get("/").relativize(target));

            if (Files.exists(result, LinkOption.NOFOLLOW_LINKS)) {
                return result.toString();
            }

            return null;
        }
    }
}
