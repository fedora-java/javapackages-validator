package org.fedoraproject.javapackages.validator.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.fedoraproject.javapackages.validator.Common;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public interface SymlinkConfig {
    /**
     * @param target The link target. Is always absolute.
     * @return Where the target was found or null if it was not found.
     */
    String targetLocation(Path target);

    public static class Envroot implements SymlinkConfig {
        private Path envroot;

        public Envroot(Path envroot) {
            this.envroot = envroot;
        }

        @Override
        @SuppressFBWarnings({"DMI_HARDCODED_ABSOLUTE_FILENAME"})
        public String targetLocation(Path target) {
            Path result = envroot.resolve(Paths.get("/").relativize(target));

            if (Files.exists(result)) {
                return result.toString();
            }

            return null;
        }
    }

    public static class RpmSet implements SymlinkConfig {
        private Map<Path, List<String>> files = new TreeMap<>();

        public RpmSet(Iterator<Path> rpms) {
            try {
                while (rpms.hasNext()) {
                    Path rpmPath = rpms.next();
                    for (var entry : Common.rpmFilesAndSymlinks(rpmPath).keySet()) {
                        files.computeIfAbsent(Common.getEntryPath(entry),
                                (path) -> new ArrayList<>()).add(rpmPath.toString());
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public String targetLocation(Path target) {
            var result = files.get(target.normalize());

            if (result != null) {
                return result.toString();
            }

            return null;
        }
    }
}
