package org.fedoraproject.javapackages.validator;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fedoraproject.javadeptools.rpm.RpmInfo;

public class RpmAttributeCheck<Config> extends ElementwiseCheck<Config> {
    protected RpmAttributeCheck(Class<Config> configClass) {
        super(configClass);
    }

    @Override
    protected Collection<String> check(Config config, RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

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
                    result.add(failMessage("{0}: Attribute {1} with invalid value: {2}",
                            textDecorate(rpm.getPath(), DECORATION_RPM),
                            textDecorate(attributeName, DECORATION_OUTER),
                            textDecorate(attributeValue, DECORATION_ACTUAL)));
                }

                if (ok) {
                    getLogger().pass("{0}: Attribute [{1}]: ok",
                            textDecorate(rpm.getPath(), DECORATION_RPM),
                            textDecorate(attributeName, DECORATION_OUTER));
                }
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }

        return result;
    }
}
