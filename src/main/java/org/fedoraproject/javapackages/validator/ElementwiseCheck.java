package org.fedoraproject.javapackages.validator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.fedoraproject.javadeptools.rpm.RpmInfo;

public abstract class ElementwiseCheck<Config> extends Check<Config> {
    public ElementwiseCheck() {
        super();
    }

    public ElementwiseCheck(Config config) {
        super(config);
    }

    abstract protected Collection<String> check(RpmInfo rpm) throws IOException;

    public final Collection<String> check(Path rpmPath) throws IOException {
        return check(new RpmInfo(rpmPath));
    }

    @Override
    public final Collection<String> check(Iterator<RpmInfo> testRpms) throws IOException {
        if (getConfig() == null) {
            System.err.println("[INFO] Configuration class not found, ignoring the test");
            return Collections.emptyList();
        }

        var result = new ArrayList<String>(0);

        while (testRpms.hasNext()) {
            result.addAll(check(testRpms.next()));
        }

        return result;
    }
}
