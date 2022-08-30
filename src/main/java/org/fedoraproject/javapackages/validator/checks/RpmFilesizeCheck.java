package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Locale;

import org.fedoraproject.javapackages.validator.Decorated;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.CheckResult;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.config.RpmFilesizeConfig;

public class RpmFilesizeCheck extends ElementwiseCheck<RpmFilesizeConfig> {
    public RpmFilesizeCheck() {
        super(RpmFilesizeConfig.class);
    }

    @Override
    public CheckResult check(RpmFilesizeConfig config, RpmPathInfo rpm) throws IOException {
        var result = new CheckResult();

        long filesize = Files.size(rpm.getPath());
        Decorated formattedFilesize = Decorated.actual(NumberFormat.getInstance(Locale.ENGLISH).format(filesize));

        if (!config.allowedFilesize(rpm, filesize)) {
            result.add("{0}: file size is: {1} bytes", Decorated.rpm(rpm), formattedFilesize);
        } else {
            getLogger().pass("{0}: file size is: {1} bytes", Decorated.rpm(rpm), formattedFilesize);
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new RpmFilesizeCheck().executeCheck(args));
    }
}
