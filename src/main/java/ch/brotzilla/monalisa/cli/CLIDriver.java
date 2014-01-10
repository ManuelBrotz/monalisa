package ch.brotzilla.monalisa.cli;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import ch.brotzilla.monalisa.cli.intf.CLICommand;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class CLIDriver {

    private final String[] args;
    private final CLIContext context;
    private Map<String, Command> commands = ImmutableMap.of();
    
    public CLIDriver(String[] args, CLIContext context) {
        Preconditions.checkNotNull(context, "The parameter 'context' must not be null");
        this.args = args;
        this.context = context;
    }

    public String[] getStartupArgs() {
        return args == null ? null : Arrays.copyOf(args, args.length);
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
                throw new IllegalStateException("Duplicate command detected (name = '" + name + "', class = '" + clazz.getSimpleName() + "', already registered class = '" + result.get(name).getClazz().getSimpleName() + "')");
            }
            final Command cmd = new Command(clazz, name, description);
            result.put(name, cmd);
        }
        commands = ImmutableMap.copyOf(result);
    }
    
    public void start() {
        
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
    }
}
