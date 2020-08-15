package ley.anvil.modpacktools.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.net.URL

class UtilTest {
    @Test
    fun sanitize() =
        assertEquals(URL("https://example.com/test%20test"), URL("https://example.com/test test").sanitize())

    @Test
    fun readAsJson() {
        val tmpDir = TemporaryFolder().apply {create()}
        val testFile = tmpDir.newFile()

        testFile.writeText(
            """
            {
                "someKey": "someValue"
            }
            """.trimIndent()
        )

        assertEquals("someValue", testFile.readAsJson()["someKey"].asString)
    }

    @Test
    fun httpPostString() {
        assertEquals(200, URL("https://postman-echo.com/post").httpPostStr("Testing").code)
    }
}
