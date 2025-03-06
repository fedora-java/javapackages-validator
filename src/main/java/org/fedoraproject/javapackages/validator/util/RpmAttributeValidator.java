package org.fedoraproject.javapackages.validator.util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.spi.Decorated;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

/**
 * Abstract class for validating specific attributes of an RPM package.
 * <p>
 * This validator dynamically retrieves and validates a specified attribute from
 * the RPM metadata using reflection.
 */
public abstract class RpmAttributeValidator extends ElementwiseValidator {

    /** The name of the RPM attribute being validated. */
    private final String attributeName;

    /**
     * Constructs an attribute validator for a specified RPM attribute.
     *
     * @param attributeName The name of the attribute to validate.
     */
    protected RpmAttributeValidator(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * Constructs an attribute validator with a filtering condition and an attribute
     * name.
     *
     * @param filter        A predicate used to determine whether validation should
     *                      be applied.
     * @param attributeName The name of the attribute to validate.
     */
    protected RpmAttributeValidator(Predicate<RpmInfo> filter, String attributeName) {
        super(filter);
        this.attributeName = attributeName;
    }

    /**
     * Checks whether the specified attribute value is allowed for the given RPM
     * package.
     *
     * @param rpm   The RPM package metadata.
     * @param value The attribute value to validate.
     * @return {@code true} if the attribute value is allowed, {@code false}
     *         otherwise.
     */
    public abstract boolean allowedAttribute(RpmInfo rpm, String value);

    /**
     * Validates the specified attribute of an RPM package.
     *
     * @param rpm The RPM package to validate.
     * @throws Exception If an error occurs during reflection or validation.
     */
    @Override
    public void validate(RpmPackage rpm) throws Exception {
        try {
            Method getter = RpmInfo.class.getMethod("get" + attributeName);

            for (Object attributeObject : List.class.cast(getter.invoke(rpm))) {
                var attributeValue = String.class.cast(attributeObject);
                boolean ok = true;

                if (!Boolean.class.cast(allowedAttribute(rpm.getInfo(), attributeValue))) {
                    ok = false;
                    fail("{0}: Attribute {1} with invalid value: {2}",
                            Decorated.rpm(rpm),
                            Decorated.outer(attributeName),
                            Decorated.actual(attributeValue));
                }

                if (ok) {
                    pass("{0}: Attribute [{1}]: ok",
                            Decorated.rpm(rpm),
                            Decorated.outer(attributeName));
                }
            }
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
