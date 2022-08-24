package org.fedoraproject.javapackages.validator;

import java.util.List;

import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;

public class Decorated {

    private static final Decoration DECORATION_RPM = Decoration.bright_red;
    private static final Decoration DECORATION_EXPECTED = Decoration.bright_cyan;
    private static final Decoration DECORATION_ACTUAL = Decoration.bright_magenta;

    /**
     * Decoration used for describing objects which hold inspected values
     */
    private static final Decoration DECORATION_OUTER = Decoration.bright_blue;

    private final Object object;
    private final Decoration[] decorations;

    private Decorated(Object object, Decoration... decorations) {
        this.object = object;
        this.decorations = decorations;
    }

    public static Decorated rpm(RpmPathInfo rpm) {
        return new Decorated(rpm, DECORATION_RPM);
    }

    public static Decorated actual(Object actual) {
        return new Decorated(actual, DECORATION_ACTUAL);
    }

    public static Decorated expected(Object expected) {
        return new Decorated(expected, DECORATION_EXPECTED);
    }

    public static Decorated outer(Object outer) {
        return new Decorated(outer, DECORATION_OUTER);
    }

    public static Decorated list(List<?> list) {
        return new Decorated(list.toString());
    }

    public static Decorated plain(Object obj) {
        return new Decorated(obj);
    }

    public static Decorated custom(Object obj, Decoration... decorations) {
        return new Decorated(obj, decorations);
    }

    public String toString() {
        return Main.getDecorator().decorate(object, decorations);
    }
}
