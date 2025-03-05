package org.fedoraproject.javapackages.validator;

import java.util.Objects;

import org.apache.commons.text.StringEscapeUtils;
import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.spi.Decoration;

/**
 * A {@code TextDecorator} implementation that formats text as HTML with
 * appropriate decorations and escaping.
 */
class HtmlDecorator implements TextDecorator {

    /** Singleton instance of {@code HtmlDecorator}. */
    public static final HtmlDecorator INSTANCE = new HtmlDecorator();

    /**
     * Private constructor to enforce singleton usage.
     */
    private HtmlDecorator() {
        super();
    }

    /**
     * Decorates the given text as an HTML element with appropriate styling.
     *
     * @param decorated the decorated text object containing formatting information
     * @return an HTML-formatted string representation of the decorated text
     */
    @Override
    public String decorate(Decorated decorated) {
        var result = new StringBuilder("<text class=\"");
        result.append(decorated.getDecoration().color().orElse(Decoration.Color.black));

        for (var modifier : decorated.getDecoration().modifiers()) {
            result.append(" ").append(modifier);
        }

        result.append("\">");
        result.append(escape(Objects.toString(decorated.getObject())));
        result.append("</text>");

        return result.toString();
    }

    /**
     * Escapes special HTML characters in the given text and replaces line breaks
     * with HTML line breaks.
     *
     * @param text the input text to escape
     * @return the escaped HTML-safe text
     */
    @Override
    public String escape(String text) {
        return StringEscapeUtils.escapeHtml4(text).replace(System.lineSeparator(), "<br>");
    }
}
