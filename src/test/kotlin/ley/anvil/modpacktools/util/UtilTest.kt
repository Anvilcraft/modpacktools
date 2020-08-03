package ley.anvil.modpacktools.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.net.URL

class UtilTest {
    @Test
    fun testSanitize() = assertEquals(URL("https://example.com/test%20test"), URL("https://example.com/test test").sanitize())

    @Test
    fun testMergeTo() = assertEquals(File("testing/dir"), File("testing") mergeTo File("dir"))
}