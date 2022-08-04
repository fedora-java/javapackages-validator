package org.fedoraproject.javapackages.validator;

import java.util.EnumMap;

public class AnsiDecorator implements TextDecorator {
    public static final AnsiDecorator INSTANCE = new AnsiDecorator();

    private AnsiDecorator() {
        super();
    }

    private static final EnumMap<Decoration, String> DECORATIONS = new EnumMap<>(Decoration.class);
    static {
        DECORATIONS.put(Decoration.bold, "1;");
        DECORATIONS.put(Decoration.underline, "4;");
        DECORATIONS.put(Decoration.black, "30m");
        DECORATIONS.put(Decoration.red, "31m");
        DECORATIONS.put(Decoration.green, "32m");
        DECORATIONS.put(Decoration.yellow, "33m");
        DECORATIONS.put(Decoration.blue, "34m");
        DECORATIONS.put(Decoration.magenta, "35m");
        DECORATIONS.put(Decoration.cyan, "36m");
        DECORATIONS.put(Decoration.white, "37m");

        DECORATIONS.put(Decoration.bright_black, "90m");
        DECORATIONS.put(Decoration.bright_red, "91m");
        DECORATIONS.put(Decoration.bright_green, "92m");
        DECORATIONS.put(Decoration.bright_yellow, "93m");
        DECORATIONS.put(Decoration.bright_blue, "94m");
        DECORATIONS.put(Decoration.bright_magenta, "95m");
        DECORATIONS.put(Decoration.bright_cyan, "96m");
        DECORATIONS.put(Decoration.bright_white, "97m");
    }

    @Override
    public String decorate(Object object, Decoration... decorations) {
        Decoration color = null;

        for (var decoration : decorations) {
            if (decoration != Decoration.bold && decoration != Decoration.underline) {
                if (color != null) {
                    throw new IllegalArgumentException("Multiple colors specified");
                } else {
                    color = decoration;
                }
            }
        }

        if (color == null) {
            color = Decoration.white;
        }

        StringBuilder result = new StringBuilder("\033[");

        for (var decoration : decorations) {
            switch (decoration) {
            case bold:
            case underline:
                result.append(DECORATIONS.get(decoration));
                break;
            default:
                continue;
            }
        }

        switch (color) {
        case black:
        case red:
        case green:
        case yellow:
        case blue:
        case magenta:
        case cyan:
        case white:
        case bright_black:
        case bright_red:
        case bright_green:
        case bright_yellow:
        case bright_blue:
        case bright_magenta:
        case bright_cyan:
        case bright_white:
            result.append(DECORATIONS.get(color));
            break;
        default:
            break;
        }

        result.append(object.toString());
        result.append("\033[0m");

        return result.toString();
    }
}
