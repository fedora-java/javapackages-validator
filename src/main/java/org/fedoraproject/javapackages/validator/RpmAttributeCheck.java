package org.fedoraproject.javapackages.validator;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fedoraproject.javadeptools.rpm.RpmInfo;
import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;
import org.fedoraproject.javapackages.validator.config.RpmPackage;

public class RpmAttributeCheck<Config> extends ElementwiseCheck<Config> {
    protected RpmAttributeCheck(Class<Config> configClass, Config config) {
        super(configClass, config);
    }

    @Override
    protected Collection<String> check(RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        String attributeName = getConfigClass().getSimpleName();
        // Remove "Config" suffix
        attributeName = attributeName.substring(0, attributeName.length() - 6);

        try {
            Method getter = RpmInfo.class.getMethod("get" + attributeName);
            Method filter = getConfig().getClass().getMethod("allowed" + attributeName, RpmPackage.class, String.class);

            for (Object attributeObject : List.class.cast(getter.invoke(rpm))) {
                var attributeValue = String.class.cast(attributeObject);
                boolean ok = true;

                if (!Boolean.class.cast(filter.invoke(getConfig(), rpm.getRpmPackage(), attributeValue))) {
                    ok = false;
                    result.add(failMessage("{0}: Attribute {1} with invalid value: {2}",
                            Main.getDecorator().decorate(rpm.getPath(), Decoration.bright_red),
                            Main.getDecorator().decorate(attributeName, Decoration.bright_cyan),
                            Main.getDecorator().decorate(attributeValue, Decoration.bright_magenta)));
                }

                if (ok) {
                    getLogger().pass("{0}: Attribute [{1}]: ok",
                            Main.getDecorator().decorate(rpm.getPath(), Decoration.bright_red),
                            Main.getDecorator().decorate(attributeName, Decoration.bright_cyan));
                }
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }

        return result;
    }
}
