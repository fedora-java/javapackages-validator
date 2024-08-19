package org.fedoraproject.javapackages.validator;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import org.fedoraproject.javapackages.validator.spi.Decorated;

class AnsiDecorator implements TextDecorator {
    public static final AnsiDecorator INSTANCE = new AnsiDecorator();

    private AnsiDecorator() {
        super();
    }

    @Override
    public String decorate(Decorated decorated) {
        var result = new StringBuilder("\033[");

        var colorCode = decorated.getDecoration().color().map(color -> switch (color) {
            case black -> 30;
            case red -> 31;
            case green -> 32;
            case yellow -> 33;
            case blue -> 34;
            case magenta -> 35;
            case cyan -> 36;
            case white -> 37;
        });

        var modifiers = new ArrayList<String>(4);
        for (var modifier : decorated.getDecoration().modifiers()) {
            switch (modifier) {
                case bold -> {modifiers.add("1");}
                case underline -> {modifiers.add("4");}
                case bright -> {colorCode = colorCode.map(color -> color + 60);}
            }
        }

        colorCode.ifPresent(color -> modifiers.add(color.toString()));
        result.append(modifiers.stream().collect(Collectors.joining(";")));
        result.append("m");

        result.append(Objects.toString(decorated.getObject()));
        result.append("\033[0m");

        return result.toString();
    }
}
