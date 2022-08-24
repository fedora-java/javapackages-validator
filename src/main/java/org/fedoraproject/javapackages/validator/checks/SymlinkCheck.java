package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.Decorated;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;
import org.fedoraproject.javapackages.validator.config.SymlinkConfig;

/**
 * Ignores source rpms.
 */
public class SymlinkCheck extends ElementwiseCheck<SymlinkConfig> {
    public SymlinkCheck() {
        super(SymlinkConfig.class);
        setFilter(Predicate.not(RpmInfo::isSourcePackage));
    }

    @Override
    protected Collection<String> check(SymlinkConfig config, RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        for (var entry : Common.rpmFilesAndSymlinks(rpm.getPath()).entrySet()) {
            Path link = Common.getEntryPath(entry.getKey());
            Path target = entry.getValue();

            if (target != null) {
                // Resolve relative links of RPM entries
                target = link.resolveSibling(target);

                String location = config.targetLocation(target);

                if (location == null) {
                    result.add(failMessage("{0}: Link {1} points to {2} (normalized as {3}) which was not found",
                            Decorated.rpm(rpm.getPath()),
                            Decorated.custom(link, Decoration.bright_cyan),
                            Decorated.custom(target, Decoration.bright_magenta),
                            Decorated.custom(target.normalize(), Decoration.magenta)));
                } else {
                    getLogger().pass("{0}: Link {1} points to {2} (normalized as {3}) located in {4}",
                            Decorated.rpm(rpm.getPath()),
                            Decorated.custom(link, Decoration.bright_cyan),
                            Decorated.custom(target, Decoration.bright_magenta),
                            Decorated.custom(target.normalize(), Decoration.magenta),
                            Decorated.outer(location));
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new SymlinkCheck().executeCheck(args));
    }
}
