package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.RpmPackageImpl;
import org.fedoraproject.javapackages.validator.config.RpmFilesizeConfig;

public class RpmFilesizeCheck extends ElementwiseCheck<RpmFilesizeConfig> {
    public RpmFilesizeCheck() {
        this(null);
    }

    public RpmFilesizeCheck(RpmFilesizeConfig config) {
        super(config);
    }

    @Override
    public Collection<String> check(RpmInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        long filesize = Files.size(rpm.getPath());
        String formattedFilesize = NumberFormat.getInstance(Locale.ENGLISH).format(filesize);

        if (!getConfig().allowedFilesize(new RpmPackageImpl(rpm), filesize)) {
            result.add(MessageFormat.format("[FAIL] {0}: file size is: {1} bytes", rpm.getPath(), formattedFilesize));
        } else {
            System.err.println(MessageFormat.format("[INFO] {0}: file size is: {1} bytes", rpm.getPath(), formattedFilesize));
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new RpmFilesizeCheck().executeCheck(RpmFilesizeConfig.class, args));
    }
}
