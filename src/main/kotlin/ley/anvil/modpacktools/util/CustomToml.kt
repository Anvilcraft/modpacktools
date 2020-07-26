package ley.anvil.modpacktools.util

import com.moandjiezana.toml.Toml
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible

class CustomToml : Toml() {
    companion object {
        @JvmStatic
        fun Toml.get(key: String): Any? {
            //Getting Around things being private for no reason 101 (dont look :P)
            val getFunc = this::class.functions.reduce {a, b -> if(b.name == "get") b else a}
            getFunc.isAccessible = true
            return getFunc.call(this, key)
        }
    }

    fun <T> getPath(path: String): T = getPath(*path.split('/', '.', '\\').toTypedArray())

    @Suppress("UNCHECKED_CAST")
    fun <T> getPath(vararg path: String): T {
        var toml: Toml = this
        path.slice(0..path.size - 2).forEach {toml = toml.getTable(it)}
        return toml.get(path[path.size - 1]) as T
    }
}