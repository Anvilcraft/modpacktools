package ley.anvil.modpacktools.command;

import ley.anvil.modpacktools.Main;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class CommandLoader {
    private final String commandsPkg;
    private final Map<String, ICommand> commands = new HashMap<>();

    /**
     * The command loader will scan the given package for {@link ICommand} classes and add them to the command list
     *
     * @param commandsPkg The package to scan for commands
     */
    public CommandLoader(String commandsPkg) {
        this.commandsPkg = commandsPkg;
        loadCommands();
    }

    private void loadCommands() {
        Reflections reflections = new Reflections(commandsPkg, new SubTypesScanner(false));
        //Get ICommands in package
        reflections.getSubTypesOf(ICommand.class).stream()
                //Only use ones with @LoadCommand annotation
                .filter(t -> t.isAnnotationPresent(LoadCommand.class))
                //Add to Command List
                .forEach(this::addClass);
    }

    /**
     * Runs the given command
     *
     * @param name the name of the command to be run
     * @param args the arguments passed into the command
     * @return the return of the command
     * @throws NoSuchElementException if there's no command with the given name
     */
    public CommandReturn runCommand(String name, String[] args) throws NoSuchElementException {
        ICommand cmd = commands.computeIfAbsent(name.toLowerCase(), x -> {
            throw new NoSuchElementException("Command " + x + " Not Found");
        });
        return runStatic(cmd, args);
    }

    public static CommandReturn runStatic(ICommand cmd, String[] args) {
        if(cmd.needsConfig() && !Main.CONFIG.configExists())
            return CommandReturn.fail("Config is needed for this command yet it is not present. Run \'init\' to generate");
        return cmd.execute(args);
    }

    /**
     * Gets all commands
     *
     * @return the commands
     */
    public Map<String, ICommand> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

    /**
     * Adds a command to the command list with the name that getName() returns
     *
     * @param command the command to add
     * @return if it was successful
     */
    public boolean addCommand(ICommand command) {
        if(commands.containsKey(command.getName()))
            return false;
        commands.put(command.getName(), command);
        return true;
    }

    /**
     * Creates a new instance of the given class and adds it to the command list
     *
     * @param clazz the class to add
     * @return if it was successful
     */
    public boolean addClass(Class<? extends ICommand> clazz) {
        try {
            return addCommand(clazz.newInstance());
        }catch(InstantiationException | IllegalAccessException e) {
            return false;
        }
    }
}
