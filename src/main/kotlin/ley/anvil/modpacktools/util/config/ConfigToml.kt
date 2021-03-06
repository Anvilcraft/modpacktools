package ley.anvil.modpacktools.util.config

import com.moandjiezana.toml.Toml
import ley.anvil.modpacktools.util.getFun

class ConfigToml : Toml() {
    companion object {
        @JvmStatic
        fun Toml.get(key: String): Any? {
            //Getting Around things being private for no reason 101 (dont look :P)
            val getFunc by lazy {Toml::class.getFun("get")}
            return getFunc?.call(this, key)
        }
    }

    /**
     * gets a path from a config.
     * when getting an [Int] do NOT supply int to [T]. instead supply [Long] and then convert to [Int]!
     *
     * @param T what to get from the config
     * @param path the path to get from the config separated by /, . or \
     */
    fun <T> getPath(path: String): T? = getPath(*path.split('/', '.', '\\').toTypedArray())

    /**
     * gets a path from a config.
     * when getting an [Int] do NOT supply int to [T]. instead supply [Long] and then convert to [Int]!
     *
     * @param T what to get from the config
     * @param path the path to get from the config
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getPath(vararg path: String): T? {
        var toml: Toml = this
        path.slice(0..path.size - 2).forEach {toml = toml.getTable(it) ?: return null}
        return toml.get(path[path.size - 1]) as? T
    }

    /**
     * gets a path from a config and throws a [MissingConfigValueException] if not found.
     * when getting an [Int] do NOT supply int to [T]. instead supply [Long] and then convert to [Int]!
     *
     * @param T what to get from the config
     * @param path the path to get from the config separated by /, . or \
     * @param message an optional message to provide the exception
     */
    fun <T> pathOrException(path: String, message: String? = null): T = getPath(path) ?: throw MissingConfigValueException(path, message)

    /**
     * gets a path from a config and throws a [MissingConfigValueException] if not found.
     * when getting an [Int] do NOT supply int to [T]. instead supply [Long] and then convert to [Int]!
     *
     * @param T what to get from the config
     * @param path the path to get from the config
     * @param message an optional message to provide the exception
     */
    fun <T> pathOrException(vararg path: String, message: String? = null): T = getPath(*path) ?: throw MissingConfigValueException(path.joinToString("/"), message)
}
