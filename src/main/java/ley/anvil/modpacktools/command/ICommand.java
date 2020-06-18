package ley.anvil.modpacktools.command;

public interface ICommand {

    /**
     * Executes this Command
     * @param args Arguments for the Command
     * @return If the Command was executed successful
     */
    CommandReturn execute(String[] args);

    default String getName() {
        return this.getClass().getAnnotation(LoadCommand.class).value();
    }
}
