package org.fedoraproject.javapackages.validator;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.config.RpmPackage;

public class RpmAttributeCheck<Config> extends ElementwiseCheck<Config> {
    protected RpmAttributeCheck(Config config) {
        super(config);
    }

    @Override
    protected Collection<String> check(RpmInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        String attributeName = getDeclaredConfigClass().getSimpleName();
        // Remove "Config" suffix
        attributeName = attributeName.substring(0, attributeName.length() - 6);

        try {
            Method getter = RpmInfo.class.getMethod("get" + attributeName);
            Method filter = getConfig().getClass().getMethod("allowed" + attributeName, RpmPackage.class, String.class);

            for (Object attributeObject : List.class.cast(getter.invoke(rpm))) {
                var attributeValue = String.class.cast(attributeObject);
                boolean ok = true;

                if (!Boolean.class.cast(filter.invoke(getConfig(), new RpmPackageImpl(rpm), attributeValue))) {
                    ok = false;
                    result.add(MessageFormat.format("[FAIL] {0}: Attribute [{1}] with invalid value: {2}",
                            rpm.getPath(), attributeName, attributeValue));
                }

                if (ok) {
                    System.err.println(MessageFormat.format("[INFO] {0}: Attribute [{1}]: ok",
                            rpm.getPath(), attributeName));
                }
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }

        return result;
    }
}
