package ley.anvil.modpacktools.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class CLIUtilTest {
    private fun prntStr(f: PrintStream.() -> Unit): String =
        ByteArrayOutputStream().apply {
            PrintStream(this, true, "UTF-8").use(f)
        }.use {it.toString("UTF-8")}

    @Test
    fun fPrintLn() {
        assertEquals(
            "Test String Formatted\n",
            prntStr {this.fPrintln("Test String", {"$it Formatted"})}
        )
    }

    @Test
    fun `fPrintLn with null`() {
        assertEquals(
            "null\n",
            prntStr {this.fPrintln(null)}
        )
    }
}
