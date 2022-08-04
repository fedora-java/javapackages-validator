package org.fedoraproject.javapackages.validator;

public interface TextDecorator {
    public static final TextDecorator NO_DECORATOR = (object, decorations) -> object.toString();

    public enum Decoration
    {
        bold, underline,

        black, red, green, yellow, blue, magenta, cyan, white,

        bright_black, bright_red, bright_green, bright_yellow,
        bright_blue, bright_magenta, bright_cyan, bright_white,
        ;
    }

    /**
     * @param object The object to decorate.
     * @param decorations Decorations to be applied to the string. May contain
     * at most one color value.
     * @return Decorated string.
     */
    String decorate(Object object, Decoration... decorations);
}
