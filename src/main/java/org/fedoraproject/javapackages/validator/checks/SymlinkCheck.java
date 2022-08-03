package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.config.SymlinkConfig;

/**
 * Ignores source rpms.
 */
public class SymlinkCheck extends ElementwiseCheck<SymlinkConfig> {
    public SymlinkCheck() {
        this(null);
    }

    public SymlinkCheck(SymlinkConfig config) {
        super(SymlinkConfig.class, config);
        setFilter((rpm) -> !rpm.getInfo().isSourcePackage());
    }

    @Override
    protected Collection<String> check(RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        for (var entry : Common.rpmFilesAndSymlinks(rpm.getPath()).entrySet()) {
            Path link = Common.getEntryPath(entry.getKey());
            Path target = entry.getValue();

            if (target != null) {
                // Resolve relative links of RPM entries
                target = link.resolveSibling(target);

                String location = getConfig().targetLocation(target);

                if (location == null) {
                    result.add(MessageFormat.format("[FAIL] {0}: Link {1} points to {2} (normalized as {3}) which was not found",
                            rpm.getPath(), link, target, target.normalize()));
                } else {
                    System.err.println(MessageFormat.format("[INFO] {0}: Link {1} points to {2} (normalized as {3}) located in {4}",
                            rpm.getPath(), link, target, target.normalize(), location));
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new SymlinkCheck().executeCheck(args));
    }
}
