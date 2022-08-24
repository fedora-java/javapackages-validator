package org.fedoraproject.javapackages.validator.checks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.fedoraproject.javadeptools.rpm.RpmArchiveInputStream;
import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.Decorated;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.config.FilesConfig;

public class FilesCheck extends ElementwiseCheck<FilesConfig> {
    public FilesCheck() {
        super(FilesConfig.class);
    }

    @Override
    public Collection<String> check(FilesConfig config, RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        try (var is = new RpmArchiveInputStream(rpm.getPath())) {
            var previousSize = result.size();
            for (CpioArchiveEntry rpmEntry; ((rpmEntry = is.getNextEntry()) != null);) {
                Path entryName = Common.getEntryPath(rpmEntry);

                if (!config.allowedFile(rpm, entryName)) {
                    result.add(failMessage("{0}: Illegal file: {1}",
                            Decorated.rpm(rpm.getPath()),
                            Decorated.actual(entryName)));
                }
            }

            if (previousSize == result.size()) {
                getLogger().pass("{0}: ok", Decorated.rpm(rpm.getPath()));
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new FilesCheck().executeCheck(args));
    }
}
