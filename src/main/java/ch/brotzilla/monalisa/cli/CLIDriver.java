package ch.brotzilla.monalisa.cli;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import jline.console.ConsoleReader;

import org.reflections.Reflections;

import ch.brotzilla.monalisa.cli.exceptions.CLIBadArgumentsException;
import ch.brotzilla.monalisa.cli.exceptions.CLICommandException;
import ch.brotzilla.monalisa.cli.exceptions.CLIExitException;
import ch.brotzilla.monalisa.cli.exceptions.CLICommandInstanciationException;
import ch.brotzilla.monalisa.cli.exceptions.CLIUnknownCommandException;
import ch.brotzilla.monalisa.cli.input.CLIJlineReader;
import ch.brotzilla.monalisa.cli.input.CLIScannerReader;
import ch.brotzilla.monalisa.cli.input.CLIStartupArgsReader;
import ch.brotzilla.monalisa.cli.intf.CLICommand;
import ch.brotzilla.monalisa.cli.intf.CLICommandInfo;
import ch.brotzilla.monalisa.cli.intf.CLIReader;

import com.beust.jcommander.JCommander;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class CLIDriver {

    private static final String PROMPT = "monalisa> ";

    private final String[] startupArgs;
    private final CLIContext context;
    private Map<String, CmdInfo> commands = ImmutableMap.of();

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

    public Map<String, CmdInfo> getCommands() {
        return commands;
    }

    public void loadCommands() {
        final Reflections ref = new Reflections("ch.brotzilla.monalisa");
        final Set<Class<? extends CLICommand>> cmds = ref.getSubTypesOf(CLICommand.class);
        final Map<String, CmdInfo> result = Maps.newHashMap();
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
                        + result.get(name).getCmdClass().getSimpleName() + "')");
            }
            final CmdInfo cmd = new CmdInfo(clazz, name, description);
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
                } catch (CLICommandException e) {
                    System.out.println(e.getMessage());
                } catch (CLIExitException e) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
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

    public static class CmdInfo {

        private final Class<? extends CLICommand> clazz;
        private final String name, description;

        public CmdInfo(Class<? extends CLICommand> clazz, String name, String description) {
            Preconditions.checkNotNull(clazz, "The parameter 'clazz' must not be null");
            Preconditions.checkNotNull(name, "The parameter 'name' must not be null");
            Preconditions.checkArgument(!name.trim().isEmpty(), "The parameter 'name' must not be empty");
            this.clazz = clazz;
            this.name = name;
            this.description = description == null || description.trim().isEmpty() ? "" : description;
        }

        public Class<? extends CLICommand> getCmdClass() {
            return clazz;
        }

        public String getCmdName() {
            return name;
        }

        public String getCmdDescription() {
            return description;
        }

        public JCommander parse(CLICommand cmd, String[] args) throws CLIBadArgumentsException {
            final JCommander commander = new JCommander(cmd);
            commander.setProgramName(getCmdName());
            try {
                if (args != null) {
                    commander.parse(args);
                }
                return commander;
            } catch (Exception e) {
                throw new CLIBadArgumentsException(getCmdName(), commander, e);
            }
        }

        public CLICommand instanciate() throws CLICommandInstanciationException {
            try {
                return getCmdClass().newInstance();
            } catch (Exception e) {
                throw new CLICommandInstanciationException(getCmdName(), e);
            }
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

        final String cmdName = args[0];
        final String[] cmdArgs = stripCmdName(args);
        final boolean isHelp = cmdArgs != null && cmdArgs.length == 1 && cmdArgs[0].equals("--help");
        final CmdInfo cmdInfo = commands.get(cmdName);

        if (cmdInfo == null) {
            throw new CLIUnknownCommandException(cmdName);
        }

        final CLICommand cmd = cmdInfo.instanciate();
        final JCommander commander = cmdInfo.parse(cmd, isHelp ? null : cmdArgs);

        if (isHelp) {
            commander.usage();
        } else {
            cmd.execute(context);
        }
    }

    private String[] stripCmdName(String[] args) {
        if (args == null || args.length < 2) {
            return null;
        }
        final String[] result = new String[args.length - 1];
        System.arraycopy(args, 1, result, 0, result.length);
        return result;
    }
}
