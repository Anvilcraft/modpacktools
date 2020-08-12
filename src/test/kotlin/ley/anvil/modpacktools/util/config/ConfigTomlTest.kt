package ley.anvil.modpacktools.util.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConfigTomlTest {
    val toml = ConfigToml().read(
        """
            [SomeCategory]
                someValue = 123
        """.trimIndent()
    ) as ConfigToml

    @Test
    fun testGetPath() {
        //String getPath
        assertEquals(
            123L,
            toml.getPath("SomeCategory/someValue")!!
        )

        //should be null if invalid category
        assertNull(toml.getPath("NonExistentCategory/val"))

        //should be null if invalid value
        assertNull(toml.getPath("SomeCategory/val"))
    }

    //should throw exception
    @Test(expected = MissingConfigValueException::class)
    fun testPathOrExceptionMissing() {
        toml.pathOrException<Long>("Asd/asd")
    }

    @Test
    fun testPathOrException() {
        assertEquals(
            123L,
            toml.pathOrException("SomeCategory/someValue")
        )
    }
}
