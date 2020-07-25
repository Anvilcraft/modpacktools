package ley.anvil.modpacktools.command

/**
 * Tells The {@link CommandLoader} to load this command
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class LoadCommand {}