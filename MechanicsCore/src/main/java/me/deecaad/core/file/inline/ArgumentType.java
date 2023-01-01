package me.deecaad.core.file.inline;

public interface ArgumentType<T> {

    /**
     * Parses the string into a valid object.
     *
     * @param str The non-null string input.
     * @return The parsed output.
     * @throws InlineException If there is a formatting error in the string.
     */
    T serialize(String str) throws InlineException;

    /**
     * Returns a <b>valid</b> string example value to be used in error
     * messages. For example, the {@link me.deecaad.core.file.inline.types.EnumType}
     * returns the name of an enum.
     *
     * <p>Consider <i>"Spicing up"</i> your examples by returning a random
     * example from a list, or generate an "ideal" example.
     *
     * @return The non-null string representation of a valid value.
     */
    String example();

    /**
     * Returns <code>true</code> if this argument type is "complex", or cannot
     * be serialized directly from a string. This is the:
     * <ul>
     *     <li>{@link me.deecaad.core.file.inline.types.NestedType}</li>
     *     <li>{@link me.deecaad.core.file.inline.types.RegistryType}</li>
     *     <li>{@link me.deecaad.core.file.inline.types.ListType}</li>
     * </ul>
     *
     * @return True if this type is complex.
     */
    default boolean isComplex() {
        return false;
    }
}
