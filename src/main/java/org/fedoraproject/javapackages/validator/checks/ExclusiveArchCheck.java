package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.Main;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;
import org.fedoraproject.javapackages.validator.config.ExclusiveArchConfig;

public class ExclusiveArchCheck extends ElementwiseCheck<ExclusiveArchConfig> {
    public ExclusiveArchCheck() {
        this(null);
    }

    public ExclusiveArchCheck(ExclusiveArchConfig config) {
        super(ExclusiveArchConfig.class, config);
        setFilter((rpm) -> rpm.getInfo().isSourcePackage());
    }

    @Override
    protected Collection<String> check(RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        if (!getConfig().allowedExclusiveArch(rpm.getRpmPackage(), rpm.getInfo().getExclusiveArch())) {
            result.add(failMessage("{0}: ExclusiveArch with values {1} failed",
                    Main.getDecorator().decorate(rpm.getPath(), Decoration.bright_red),
                    Main.getDecorator().decorate(rpm.getInfo().getExclusiveArch(), Decoration.bright_cyan)));
        } else {
            getLogger().pass("{0}: ExclusiveArch with values {1} passed",
                    Main.getDecorator().decorate(rpm.getPath(), Decoration.bright_red),
                    Main.getDecorator().decorate(rpm.getInfo().getExclusiveArch(), Decoration.bright_cyan));
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new ExclusiveArchCheck().executeCheck(args));
    }
}
