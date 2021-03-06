@file:JvmName("CLIUtil")

package ley.anvil.modpacktools.util

import java.io.PrintStream

/**
 * applies all given [formatters] to the [toString] representation of [x] and then prints it to [System.out]
 *
 * @param x the object to print
 * @param formatters the formatters to apply to [x]. they will be ran in the order they are supplied in
 */
fun fPrint(x: Any?, vararg formatters: (String) -> String) = System.out.fPrint(x, *formatters)

/**
 * applies all given [formatters] to the [toString] representation of [x] and then prints it
 *
 * @receiver the [PrintStream] to print to
 * @param x the object to print
 * @param formatters the formatters to apply to [x]. they will be ran in the order they are supplied in
 */
fun PrintStream.fPrint(x: Any?, vararg formatters: (String) -> String) =
    this.print(formatters.fold(x.toString()) {acc, f -> f(acc)})

/**
 * applies all given [formatters] to the [toString] representation of [x] and then prints it with a newline at the end
 *
 * @receiver the [PrintStream] to print to
 * @param x the object to print
 * @param formatters the formatters to apply to [x]. they will be ran in the order they are supplied in
 */
fun PrintStream.fPrintln(x: Any?, vararg formatters: (String) -> String) = this.fPrint(x, *formatters, {"$it\n"})

/**
 * applies all given [formatters] to the [toString] representation of [x] and then prints it to [System.out] with a newline at the end
 *
 * @param x the object to print
 * @param formatters the formatters to apply to [x]. they will be ran in the order they are supplied in
 */
fun fPrintln(x: Any?, vararg formatters: (String) -> String) = System.out.fPrintln(x, *formatters)
