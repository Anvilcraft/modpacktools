package ley.anvil.modpacktools.command;

/**
 * This must be implemented by all commands
 */
public interface ICommand {
    /**
     * Executes this Command
     *
     * @param args Arguments for the Command
     * @return If the Command was executed successful
     */
    CommandReturn execute(String[] args);

    /**
     * This should return the name of the command (the return should always be the same when this method is called)
     *
     * @return command name
     */
    String getName();

    /**
     * If this command needs the config file to be present. the command will not run if this returns true and there is no config file
     *
     * @return if a config file is required
     */
    default boolean needsConfig() {
        return true;
    }

    /**
     * This message will be displayed in the help dialog (This shold always return the same value)
     *
     * @return the help message
     */
    default String getHelpMessage() {
        return "";
    }
}
