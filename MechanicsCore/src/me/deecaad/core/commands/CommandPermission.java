package me.deecaad.core.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandPermission {

    /**
     * Returns the string representation of the
     * annotated <code>SubCommand</code>'s
     * permission
     *
     * Ex. essentials.fly
     *
     * @return String permission
     */
    String permission();
}
