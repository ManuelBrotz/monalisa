package ch.brotzilla.monalisa.cli;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import jline.console.ConsoleReader;

import org.reflections.Reflections;

import ch.brotzilla.monalisa.cli.exceptions.CLIException;
import ch.brotzilla.monalisa.cli.exceptions.CommandInstanciationException;
import ch.brotzilla.monalisa.cli.exceptions.UnknownCommandException;
import ch.brotzilla.monalisa.cli.input.CLIJlineReader;
import ch.brotzilla.monalisa.cli.input.CLIScannerReader;
import ch.brotzilla.monalisa.cli.input.CLIStartupArgsReader;
import ch.brotzilla.monalisa.cli.intf.CLICommand;
import ch.brotzilla.monalisa.cli.intf.CLICommandInfo;
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
        final Set<Class<? extends CLICommand>> cmds = ref.getSubTypesOf(CLICommand.class);
        final Map<String, Command> result = Maps.newHashMap();
        for (final Class<? extends CLICommand> clazz : cmds) {
            final CLICommandInfo annotation = clazz.getAnnotation(CLICommandInfo.class);
            if (annotation == null) {
                continue;
            }
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
                try {
                    processLine(nextLine, nextLine == startupArgs);
                } catch (CLIException e) {
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    
                }
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

        private final Class<? extends CLICommand> clazz;
        private final String name, description;
        
        private CLICommand createCommand() throws CommandInstanciationException {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new CommandInstanciationException(name, e);
            }
        }

        public Command(Class<? extends CLICommand> clazz, String name, String description) {
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
        
        public void execute(String[] args, CLIContext context) throws Exception {
             final CLICommand cmd = createCommand();
             cmd.execute(context);
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

    private void processLine(String[] args, boolean isStartupArgs) throws Exception {
        if (args == null || args.length == 0) {
            return;
        }

        final String commandName = args[0];
        final Command command = commands.get(commandName);
        
        if (command == null) {
            throw new UnknownCommandException(commandName);
        }
        
        command.execute(stripArgs(args), context);
    }
    
    private String[] stripArgs(String[] args) {
        if (args == null || args.length < 2) {
            return null;
        }
        final String[] result = new String[args.length-1];
        System.arraycopy(args, 1, result, 0, result.length);
        return result;
    }
}
