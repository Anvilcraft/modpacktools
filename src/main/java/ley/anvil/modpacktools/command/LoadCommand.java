package ley.anvil.modpacktools.command;

import java.lang.annotation.*;

/**
 * Tells The {@code CommandLoader} to load this command
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LoadCommand {
    /**
     * the name of this command, must be lower case
     *
     * @return the name of this command
     */
    String value();
}
