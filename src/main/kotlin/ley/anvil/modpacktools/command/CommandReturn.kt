package ley.anvil.modpacktools.command

data class CommandReturn constructor(val ret: String?, val success: Boolean) {
    companion object {
        /**
         * Get a failed [CommandReturn]. This should be used if something went wrong
         *
         * @param ret the error message
         * @return the [CommandReturn]
         */
        @JvmStatic
        @JvmOverloads
        fun fail(ret: String? = null) = CommandReturn(ret, false)

        /**
         * Get a successful [CommandReturn]. Use this if the command was executed successfully
         *
         * @param ret a return message
         * @return the [CommandReturn]
         */
        @JvmStatic
        @JvmOverloads
        fun success(ret: String? = null) = CommandReturn(ret, true)
    }
    fun hasRet() = ret != null
}
