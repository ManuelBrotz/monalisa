package ch.brotzilla.monalisa.cli;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import jline.console.ConsoleReader;

import org.reflections.Reflections;

import ch.brotzilla.monalisa.cli.input.CLIJlineReader;
import ch.brotzilla.monalisa.cli.input.CLIScannerReader;
import ch.brotzilla.monalisa.cli.input.CLIStartupArgsReader;
import ch.brotzilla.monalisa.cli.intf.CLICommand;
import ch.brotzilla.monalisa.cli.intf.CLIReader;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class CLIDriver {

    private static final String PROMPT = "monalisa> ";

    private final String[] startupArgs;
    private final CLIContext context;
    private Map<String, Command> commands = ImmutableMap.of();

    public CLIDriver(String[] args, CLIContext context) {
        Preconditions.checkNotNull(context, "The parameter 'context' must not be null");
        this.startupArgs = args == null || args.length == 0 ? null : args;
        this.context = context;
    }

    public boolean isJlineDisabled() {
        return Boolean.getBoolean("jlineDisable");
    }

    public String[] getStartupArgs() {
        return startupArgs == null ? null : Arrays.copyOf(startupArgs, startupArgs.length);
    }

    public CLIContext getContext() {
        return context;
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public void loadCommands() {
        final Reflections ref = new Reflections("ch.brotzilla.monalisa");
        final Set<Class<?>> cmds = ref.getTypesAnnotatedWith(CLICommand.class);
        final Map<String, Command> result = Maps.newHashMap();
        for (final Class<?> clazz : cmds) {
            final CLICommand annotation = clazz.getAnnotation(CLICommand.class);
            final String name = annotation.name(), description = annotation.description();
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalStateException("Command name must not be empty (class = '" + clazz.getSimpleName() + "')");
            }
            if (result.containsKey(name)) {
                throw new IllegalStateException("Duplicate command detected (name = '" + name + "', class = '" + clazz.getSimpleName() + "', already registered class = '"
                        + result.get(name).getClazz().getSimpleName() + "')");
            }
            final Command cmd = new Command(clazz, name, description);
            result.put(name, cmd);
        }
        commands = ImmutableMap.copyOf(result);
    }

    public void start() {
        try (final CLIReader reader = createReader()) {
            while (true) {
                final String[] nextLine = reader.nextLine(PROMPT);
                processLine(nextLine, nextLine == startupArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final CLIContext ctx = new CLIContext();
        final CLIDriver cli = new CLIDriver(args, ctx);
        cli.loadCommands();
        cli.start();
    }

    public static class Command {

        private final Class<?> clazz;
        private final String name, description;

        public Command(Class<?> clazz, String name, String description) {
            Preconditions.checkNotNull(clazz, "The parameter 'clazz' must not be null");
            Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
            Preconditions.checkArgument(!name.trim().isEmpty(), "The parameter 'name' must not be empty");
            this.clazz = clazz;
            this.name = name;
            this.description = description == null || description.trim().isEmpty() ? "" : description;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
        
        public void execute() throws Exception {
             
        }
    }

    private CLIReader createReader() throws Exception {
        final CLIReader reader;
        if (isJlineDisabled()) {
            final Scanner scanner = new Scanner(System.in);
            reader = new CLIScannerReader(scanner);
        } else {
            final ConsoleReader consoleReader = new ConsoleReader();
            consoleReader.setBellEnabled(false);
            reader = new CLIJlineReader(consoleReader);
        }
        if (startupArgs != null) {
            return new CLIStartupArgsReader(startupArgs, reader);
        }
        return reader;
    }

    private void processLine(String[] args, boolean isStartupArgs) {
        if (args == null || args.length == 0) {
            return;
        }

        final String commandName = args[0];
        final Command command = commands.get(commandName);
        
        if (command == null) {
            System.out.println("Unknown command: " + commandName);
            return;
        }
        
        
        
    }
}
