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

    public CommandLoader(String commandsPkg) {
        this.commandsPkg = commandsPkg;
        loadCommands();
    }

    private void loadCommands() {
        Reflections reflections = new Reflections(commandsPkg, new SubTypesScanner(false));
        reflections.getSubTypesOf(ICommand.class).stream()
                .filter(t -> t.isAnnotationPresent(LoadCommand.class))
                .forEach(t -> {
                    try {
                        commands.put(t.getAnnotation(LoadCommand.class).value(), t.newInstance());
                    }catch(InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
    }

    public CommandReturn runCommand(String name, String[] args) throws NoSuchElementException {
        return commands.computeIfAbsent(name.toLowerCase(), x -> {
            throw new NoSuchElementException("Command " + x + " Not Found");
        }).execute(args);
    }

    public Map<String, ICommand> getCommands() {
        return Collections.unmodifiableMap(commands);
    }
}
