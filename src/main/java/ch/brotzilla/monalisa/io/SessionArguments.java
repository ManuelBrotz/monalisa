package ch.brotzilla.monalisa.io;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class SessionArguments {

    private final JCommander commander;

    private String[] args;
    private boolean isValid;

    @Parameter(names = "--show-gui", description = "Displays the gui")
    private boolean showGUI;

    @ParametersDelegate
    private ActionDelegate action = new ActionDelegate();

    @ParametersDelegate
    private FilesDelegate files = new FilesDelegate();

    public static enum Action {
        Start, Resume;
    }

    public SessionArguments() {
        this.commander = new JCommander(this);
    }

    public String[] getArguments() {
        return args == null ? null : Arrays.copyOf(args, args.length);
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean getShowGUI() {
        return showGUI;
    }

    public Action getAction() {
        return action.getAction();
    }

    public List<File> getFiles() {
        return files.getFiles();
    }

    public void parseArguments(String[] args) {
        if (this.args != null) {
            throw new IllegalStateException("Session arguments have already been initialized");
        }
        this.args = args;
        try {
            commander.parse(args);
            action.validate();
            files.validate(action.getAction());
            isValid = true;
        } catch (Exception e) {
            isValid = false;
            throw e;
        }
    }

    public static void main(String[] args) {
        final SessionArguments arguments = new SessionArguments();
        arguments.parseArguments(new String[] { "--start", "test.png", "test.png" });
        System.out.println("Action: " + arguments.getAction());
        for (final File file : arguments.getFiles()) {
            System.out.println(file);
        }
    }

    private class ActionDelegate {

        @Parameter(names = "--start", description = "Starts a new session")
        private boolean start = false;

        @Parameter(names = "--resume", description = "Resumes a previous session")
        private boolean resume = false;

        public Action getAction() {
            if (start) {
                return Action.Start;
            }
            if (resume) {
                return Action.Resume;
            }
            return null;
        }

        public void validate() {
            if (start && resume) {
                throw new ParameterException("The actions --start and --resume are mutually exclusive");
            } else if (!start && !resume) {
                throw new ParameterException("One of the actions --start or --resume is required");
            }
        }
    }

    private class FilesDelegate {

        @Parameter(description = "The files")
        private List<File> files = Lists.newArrayList();

        public List<File> getFiles() {
            return ImmutableList.copyOf(files);
        }

        public void validate(Action action) {
            Preconditions.checkNotNull(action, "The parameter 'action' must not be null");
            switch (action) {
            case Start:
                if (files.size() == 0) {
                    throw new ParameterException("At least one input file is required");
                } else if (files.size() > 2) {
                    throw new ParameterException("At most two input files are allowed");
                }
                break;
            case Resume:
                if (files.size() == 0) {
                    throw new ParameterException("At least one input file is required");
                } else if (files.size() > 1) {
                    throw new ParameterException("At most one input file is allowed");
                }
                break;
            default:
                throw new ParameterException("Unsupported action: " + action);
            }
        }
    }
}
