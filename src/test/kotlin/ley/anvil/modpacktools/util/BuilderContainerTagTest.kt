package ley.anvil.modpacktools.util

import ley.anvil.modpacktools.util.BuilderContainerTag.Companion.html
import org.junit.Assert.assertEquals
import org.junit.Test

class BuilderContainerTagTest {
    @Test
    fun htmlBuilder() {
        assertEquals(
            "<html><body><h1>Hello</h1></body></html>",
            html {
                "body" {
                    "h1"("Hello")
                }
            }.render()
        )
    }
}
