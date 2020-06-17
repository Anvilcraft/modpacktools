package ley.anvil.modpacktools.commands;

public interface ICommand {

    /**
     * Executes this Command
     * @param args Arguments for the Command
     * @return If the Command was executed successful
     */
    boolean execure(String[] args);

}
