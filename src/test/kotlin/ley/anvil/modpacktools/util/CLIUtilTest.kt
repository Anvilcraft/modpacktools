package ley.anvil.modpacktools.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class CLIUtilTest {
    fun prntStr(f: PrintStream.() -> Unit): String =
        ByteArrayOutputStream().apply {
            PrintStream(this, true, "UTF-8").use(f)
        }.use {it.toString("UTF-8")}

    @Test
    fun testFPrintln() {
        assertEquals(
            "Test String Formatted\n",
            prntStr {this.fPrintln("Test String", {"$it Formatted"})}
        )
    }

    @Test
    fun testFPrintlnNull() {
        assertEquals(
            "null\n",
            prntStr {this.fPrintln(null)}
        )
    }
}
