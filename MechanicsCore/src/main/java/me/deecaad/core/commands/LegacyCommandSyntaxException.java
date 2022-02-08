package me.deecaad.core.commands;

public class LegacyCommandSyntaxException extends Exception {

    public LegacyCommandSyntaxException(String message) {
        super(message);
    }

    public LegacyCommandSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public LegacyCommandSyntaxException(Throwable cause) {
        super(cause);
    }
}
