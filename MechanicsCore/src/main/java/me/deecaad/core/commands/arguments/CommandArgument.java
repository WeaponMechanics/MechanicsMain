package me.deecaad.core.commands;

public abstract class Argument<T> {

    private boolean required;

    public Argument() {
    }

    public Argument(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    /**
     * Parses the given command argument input into whichever type this
     * argument requires. If the input is invalid (The user used a wrong type),
     * then a {@link CommandArgumentException} will be thrown.
     *
     * @param str The string input the user defined, or null (If required = false).
     * @return The parsed argument type.
     * @throws CommandArgumentException If the input is not formatted correctly.
     */
    public abstract T parse(String str) throws CommandArgumentException;
}
