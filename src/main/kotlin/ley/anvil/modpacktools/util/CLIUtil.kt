@file:JvmName("CLIUtil")

package ley.anvil.modpacktools.util

import java.io.PrintStream

/**
 * applies all given formatters to the string representation of the object and then prints it to stdout
 *
 * @param x the object to print
 * @param formatters the formatters to apply to x, they will be ran in the order they are supplied in
 */
fun fPrint(x: Any?, vararg formatters: (String) -> String) = System.out.fPrint(x, *formatters)

/**
 * applies all given formatters to the string representation of the object and then prints it
 *
 * @receiver the printStream to print to
 * @param x the object to print
 * @param formatters the formatters to apply to x, they will be ran in the order they are supplied in
 */
fun PrintStream.fPrint(x: Any?, vararg formatters: (String) -> String) {
    var str = x.toString()
    formatters.forEach {str = it(str)}
    this.print(str)
}

/**
 * applies all given formatters to the string representation of the object and then prints it with a newline at the end
 *
 * @receiver the printStream to print to
 * @param x the object to print
 * @param formatters the formatters to apply to x, they will be ran in the order they are supplied in
 */
fun PrintStream.fPrintln(x: Any?, vararg formatters: (String) -> String) = this.fPrint("${x.toString()}\n", *formatters)

/**
 * applies all given formatters to the string representation of the object and then prints it to stdout
 * with a newline at the end
 *
 * @param x the object to print
 * @param formatters the formatters to apply to x, they will be ran in the order they are supplied in
 */
fun fPrintln(x: Any?, vararg formatters: (String) -> String) = fPrint("${x.toString()}\n", *formatters)