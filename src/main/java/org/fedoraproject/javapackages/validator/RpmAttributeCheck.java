package org.fedoraproject.javapackages.validator;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.fedoraproject.javadeptools.rpm.RpmInfo;

public class RpmAttributeCheck<Config> extends ElementwiseCheck<Config> {
    protected RpmAttributeCheck(Class<Config> configClass) {
        super(configClass);
    }

    @Override
    protected CheckResult check(Config config, RpmPathInfo rpm) throws IOException {
        var result = new CheckResult();

        String attributeName = getConfigClass().getSimpleName();
        // Remove "Config" suffix
        attributeName = attributeName.substring(0, attributeName.length() - 6);

        try {
            Method getter = RpmInfo.class.getMethod("get" + attributeName);
            Method filter = config.getClass().getMethod("allowed" + attributeName, RpmInfo.class, String.class);

            for (Object attributeObject : List.class.cast(getter.invoke(rpm))) {
                var attributeValue = String.class.cast(attributeObject);
                boolean ok = true;

                if (!Boolean.class.cast(filter.invoke(config, rpm, attributeValue))) {
                    ok = false;
                    result.add("{0}: Attribute {1} with invalid value: {2}",
                            Decorated.rpm(rpm.getPath()),
                            Decorated.outer(attributeName),
                            Decorated.actual(attributeValue));
                }

                if (ok) {
                    getLogger().pass("{0}: Attribute [{1}]: ok",
                            Decorated.rpm(rpm.getPath()),
                            Decorated.outer(attributeName));
                }
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }

        return result;
    }
}
