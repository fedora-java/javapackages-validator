package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.fedoraproject.javadeptools.rpm.RpmInfo;

public abstract class ElementwiseCheck<Config> extends Check<Config> {
    public ElementwiseCheck() {
        super();
    }

    public ElementwiseCheck(Config config) {
        super(config);
    }

    abstract protected Collection<String> check(Path rpmPath, RpmInfo rpmInfo) throws IOException;

    public final Collection<String> check(Path rpmPath) throws IOException {
        return check(rpmPath, new RpmInfo(rpmPath));
    }

    private Collection<String> checkNonNull(Path rpmPath, RpmInfo rpmInfo) throws IOException {
        if (getConfig() == null) {
            System.err.println("[INFO] Configuration class not found, ignoring the test");
            return Collections.emptyList();
        }

        return check(rpmPath, rpmInfo);
    }

    @Override
    public Collection<String> check(List<Path> testRpms) throws IOException {
        var result = new ArrayList<String>(0);

        for (Path rpmPath : testRpms) {
            var rpmInfo = new RpmInfo(rpmPath);
            result.addAll(checkNonNull(rpmPath, rpmInfo));
        }

        return result;
    }
}
