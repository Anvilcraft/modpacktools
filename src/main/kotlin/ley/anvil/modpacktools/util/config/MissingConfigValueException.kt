package ley.anvil.modpacktools.util.config

/**
 * this should be thrown if an expected config value is not found
 *
 * @param missingValue the config value that is missing
 * @param message an optional message to be displayed
 */
data class MissingConfigValueException(val missingValue: String, override val message: String? = null) : IllegalStateException(message) {
    //overridden to get better looking stack trace
    override fun toString(): String = "MissingConfigValueException: Value $missingValue not found: ${message ?: ""}"
}
