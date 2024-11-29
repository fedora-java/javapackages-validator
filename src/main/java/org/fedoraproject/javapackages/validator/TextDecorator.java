package org.fedoraproject.javapackages.validator;

import java.util.Objects;

import org.fedoraproject.javapackages.validator.spi.Decorated;

/// An interface for decorating text with various styles and formatting options.
/// Implementations of this interface apply specific decorations, such as colors
/// and styles, to a given text input.
interface TextDecorator {

    /// A default `TextDecorator` instance that applies no decorations,
    /// returning the object's string representation as is.
    public static final TextDecorator NO_DECORATOR = decorated -> Objects.toString(decorated.getObject());

    /// Applies decorations to the provided `Decorated` object and returns the
    /// resulting formatted string.
    ///
    /// @param decorated The object to decorate, containing the text and associated
    ///                  decorations.
    /// @return A decorated string with applied formatting, or the original string if
    ///         no decorations are present.
    String decorate(Decorated decorated);

    /// Escapes special characters in the given text to ensure it is properly
    /// formatted. This method can be overridden by implementations to provide custom
    /// escaping logic.
    ///
    /// @param text The input text to escape.
    /// @return The escaped string, ensuring proper formatting.
    default String escape(String text) {
        return text;
    }
}
