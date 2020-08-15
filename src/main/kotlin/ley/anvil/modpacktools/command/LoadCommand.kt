package ley.anvil.modpacktools.command

/**
 * Tells The [CommandLoader] to load this [ICommand]
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class LoadCommand
