package ley.anvil.modpacktools.command;

/**
 * This must be implemented by all commands
 */
@FunctionalInterface
public interface ICommand {
    /**
     * Executes this Command
     * @param args Arguments for the Command
     * @return If the Command was executed successful
     */
    CommandReturn execute(String[] args);

    /**
     * This should return the name of the command (this usually doesn't need to be overwritten)
     *
     * @return command name
     */
    default String getName() {
        return this.getClass().getAnnotation(LoadCommand.class).value();
    }
}
