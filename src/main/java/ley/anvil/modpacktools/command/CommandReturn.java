package ley.anvil.modpacktools.command;

public class CommandReturn {
    private final String ret;
    private final boolean success;

    private CommandReturn(String ret, boolean success) {
        this.ret = ret;
        this.success = success;
    }

    /**
     * Get a failed {@link CommandReturn}. This should be used if something went wrong
     *
     * @param ret the error message
     * @return the {@link CommandReturn}
     */
    public static CommandReturn fail(String ret) {
        return new CommandReturn(ret, false);
    }

    /**
     * Get a successful {@link CommandReturn} Without a message. Use this if the command was executed successfully
     *
     * @return the {@link CommandReturn}
     */
    public static CommandReturn success() {
        return success("");
    }

    /**
     * Get a successful {@link CommandReturn} With a message. Use this if the command was executed successfully
     *
     * @return the {@link CommandReturn}
     */
    public static CommandReturn success(String ret) {
        return new CommandReturn(ret, true);
    }

    /**
     * Returns true if the command was executed successfully
     *
     * @return if the command ran successfully
     */
    public boolean hadSuccess() {
        return success;
    }

    /**
     * Get the Return Message of the command (may be empty String)
     *
     * @return the return message
     */
    public String getRet() {
        return ret;
    }
}
