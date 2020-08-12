package ley.anvil.modpacktools.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.URL

class UtilTest {
    @Test
    fun testSanitize() = assertEquals(URL("https://example.com/test%20test"), URL("https://example.com/test test").sanitize())

    @Test
    fun testMergeTo() = assertEquals(File("testing/dir"), File("testing") mergeTo File("dir"))

    @Test
    fun testReadAsJson() {
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
}
