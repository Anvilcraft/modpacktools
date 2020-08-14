package ley.anvil.modpacktools.util.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConfigTomlTest {
    private val toml = ConfigToml().read(
        """
            [SomeCategory]
                someValue = 123
        """.trimIndent()
    ) as ConfigToml

    @Test
    fun getPath() {
        //String getPath
        assertEquals(
            123L,
            toml.getPath("SomeCategory/someValue")!!
        )

        //should be null if invalid category
        assertNull(toml.getPath("NonExistentCategory/val"))
    }

    @Test
    fun `getPath with invalid path`() {
        //should be null if invalid value
        assertNull(toml.getPath("SomeCategory/val"))
    }

    //should throw exception
    @Test(expected = MissingConfigValueException::class)
    fun `pathOrException with invalid path`() {
        toml.pathOrException<Long>("Asd/asd")
    }

    @Test
    fun pathOrException() {
        assertEquals(
            123L,
            toml.pathOrException("SomeCategory/someValue")
        )
    }
}
