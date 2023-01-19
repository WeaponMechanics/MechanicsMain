package me.deecaad.core.file.inline;

/**
 * Sometimes an {@link ArgumentType} does not have <i>"enough"</i> checks to
 * get a valid argument. For example, the {@link me.deecaad.core.file.inline.types.IntegerType}
 * can check if an argument is in a set range, but not if the integer input is
 * even/odd. If you only wanted even numbers, you would use an ArgumentValidator
 * to make sure all input numbers are even.
 */
@FunctionalInterface
public interface ArgumentValidator {

    /**
     * Checks if the given value is valid for the argument.
     *
     * @param value The serialized value.
     * @throws InlineException If the value is invalid.
     */
    void validate(Object value) throws InlineException;

    default void validate(String input, Object value) throws InlineException {
        try {
            validate(value);
        } catch (InlineException ex) {
            ex.setIssue(input);
        }
    }
}
