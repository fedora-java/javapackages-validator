package org.fedoraproject.javapackages.validator;

import java.util.Objects;

import org.fedoraproject.javapackages.validator.spi.Decorated;

interface TextDecorator {
    public static final TextDecorator NO_DECORATOR = decorated -> Objects.toString(decorated.getObject());

    /**
     * @param object The object to decorate.
     * @param decorations Decorations to be applied to the string. May contain
     * at most one color value.
     * @return Decorated string.
     */
    String decorate(Decorated decorated);

    default String escape(String text) {
        return text;
    }
}
