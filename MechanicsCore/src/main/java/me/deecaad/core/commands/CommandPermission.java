package me.deecaad.core.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation outlines which permission should be used for a command. All
 * subclasses of the {@link SubCommand} class should utilise this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated()
public @interface CommandPermission {

    /**
     * Returns the non-null {@link String} permission. It should follow a
     * <samp>plugin.command.subcommand</samp> format, and the subcommand should
     * be the same as the annotated class' name.
     *
     * @return The string value of a permission
     */
    String permission();
}
