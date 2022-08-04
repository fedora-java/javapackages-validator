package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.Main;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;
import org.fedoraproject.javapackages.validator.config.RpmFilesizeConfig;

public class RpmFilesizeCheck extends ElementwiseCheck<RpmFilesizeConfig> {
    public RpmFilesizeCheck() {
        this(null);
    }

    public RpmFilesizeCheck(RpmFilesizeConfig config) {
        super(RpmFilesizeConfig.class, config);
    }

    @Override
    public Collection<String> check(RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        long filesize = Files.size(rpm.getPath());
        String formattedFilesize = NumberFormat.getInstance(Locale.ENGLISH).format(filesize);

        if (!getConfig().allowedFilesize(rpm.getRpmPackage(), filesize)) {
            result.add(failMessage("{0}: file size is: {1} bytes",
                    Main.getDecorator().decorate(rpm.getPath(), Decoration.bright_red),
                    Main.getDecorator().decorate(formattedFilesize, Decoration.bright_cyan)));
        } else {
            getLogger().pass("{0}: file size is: {1} bytes",
                    Main.getDecorator().decorate(rpm.getPath(), Decoration.bright_red),
                    Main.getDecorator().decorate(formattedFilesize, Decoration.bright_cyan));
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new RpmFilesizeCheck().executeCheck(args));
    }
}
