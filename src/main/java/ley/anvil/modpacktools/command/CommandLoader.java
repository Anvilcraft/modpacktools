package ley.anvil.modpacktools.command;

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
                //Add to HashMap
                .forEach(t -> {
                    try {
                        addCommand(t.getAnnotation(LoadCommand.class).value(), t.newInstance());
                    }catch(InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
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
        return commands.computeIfAbsent(name.toLowerCase(), x -> {
            throw new NoSuchElementException("Command " + x + " Not Found");
        }).execute(args);
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
     * Adds a command to the command list
     *
     * @param name the name of the command to add
     * @param command the command to add
     * @return if it was successful
     */
    public boolean addCommand(String name, ICommand command) {
        if(commands.containsKey(name))
            return false;
        commands.put(name, command);
        return true;
    }
}
