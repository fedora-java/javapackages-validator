package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.Main;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.TextDecorator;
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
                    result.add(failMessage("{0}: Link {1} points to {2} (normalized as {3}) which was not found",
                            Main.getDecorator().decorate(rpm.getPath(), TextDecorator.Decoration.bright_red),
                            Main.getDecorator().decorate(link, TextDecorator.Decoration.bright_cyan),
                            Main.getDecorator().decorate(target, TextDecorator.Decoration.bright_magenta),
                            Main.getDecorator().decorate(target.normalize(), TextDecorator.Decoration.magenta)));
                } else {
                    getLogger().pass("{0}: Link {1} points to {2} (normalized as {3}) located in {4}",
                            Main.getDecorator().decorate(rpm.getPath(), TextDecorator.Decoration.bright_red),
                            Main.getDecorator().decorate(link, TextDecorator.Decoration.bright_cyan),
                            Main.getDecorator().decorate(target, TextDecorator.Decoration.bright_magenta),
                            Main.getDecorator().decorate(target.normalize(), TextDecorator.Decoration.magenta),
                            Main.getDecorator().decorate(location, TextDecorator.Decoration.bright_blue));
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new SymlinkCheck().executeCheck(args));
    }
}
