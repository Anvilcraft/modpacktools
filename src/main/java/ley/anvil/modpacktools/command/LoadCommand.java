package ley.anvil.modpacktools.command;

import java.lang.annotation.*;

/**
 * Tells The {@link CommandLoader} to load this command
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LoadCommand {
}
