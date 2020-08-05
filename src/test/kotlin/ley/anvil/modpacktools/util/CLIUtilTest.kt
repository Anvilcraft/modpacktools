package ley.anvil.modpacktools.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class CLIUtilTest {
    @Test
    fun testFPrintln() {
        val baos = ByteArrayOutputStream()
        PrintStream(baos, true, "UTF-8").use {
            it.fPrintln("Test String", {s -> "$s Formatted"})
        }

        assertEquals("Test String Formatted\n", baos.toString("UTF-8"))
    }
}
