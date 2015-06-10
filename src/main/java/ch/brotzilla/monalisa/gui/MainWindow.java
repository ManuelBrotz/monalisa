package ch.brotzilla.monalisa.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileFilter;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.Monalisa;
import ch.brotzilla.monalisa.db.Database;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.gui.StatusDisplay.Orientation;
import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.rendering.GenomeRenderer;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.monalisa.vectorizer.VectorizerContext;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

    protected final Monalisa monalisa;
    protected final SessionManager sessionManager;
    protected final Image inputImage, currentImage;
    protected final Image importanceMap;
    protected final GenomeRenderer renderer;

    protected final JMenuBar menuBar;

    protected final JTabbedPane tabbedPane;
    protected final JScrollPane inputImageScrollPane, currentImageScrollPane, importanceMapScrollPane;
    protected final ImageDisplay inputImageDisplay, currentImageDisplay, importanceMapDisplay;

    protected final StatusDisplay statusDisplay;

    protected Genome currentGenome;
    protected long lastRenderTime = 0;

    protected static class Listener implements WindowListener, ComponentListener {

        public final MainWindow window;

        public Listener(MainWindow window) {
            this.window = Preconditions.checkNotNull(window, "The parameter 'window' must not be null");
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
            window.setVisible(false);
            window.monalisa.quit();
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }

        @Override
        public void componentResized(ComponentEvent e) {

        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }

    public MainWindow(Monalisa monalisa) throws IOException {
        super();

        this.monalisa = Preconditions.checkNotNull(monalisa, "The parameter 'monalisa' must not be null");
        this.sessionManager = monalisa.getSessionManager();
        this.currentGenome = monalisa.getSessionManager().getVectorizerContext().getLatestGenome();

        final VectorizerContext vc = sessionManager.getVectorizerContext();
        
        this.inputImage = new Image(vc.getTargetImage());
        if (vc.getImportanceMap() != null) {
            this.importanceMap = new Image(vc.getImportanceMap());
        } else {
            this.importanceMap = null;
        }

        this.renderer = monalisa.getVectorizer().getConfig().createRenderer();
        if (currentGenome != null) {
            renderer.render(currentGenome);
        }
        
        this.currentImage = renderer.getImage();
        
        updateWindowTitle(sessionManager.getSessionName());

        setLayout(new BorderLayout());

        this.menuBar = new JMenuBar();
        menuBar.add(buildFileMenu());
        menuBar.add(buildImageMenu());
        setJMenuBar(menuBar);

        this.tabbedPane = new JTabbedPane();

        this.inputImageDisplay = new ImageDisplay(inputImage);
        this.inputImageScrollPane = new JScrollPane(inputImageDisplay);
        tabbedPane.addTab("Input Image", inputImageScrollPane);

        this.currentImageDisplay = new ImageDisplay(currentImage);
        this.currentImageScrollPane = new JScrollPane(currentImageDisplay);
        tabbedPane.addTab("Current Image", currentImageScrollPane);

        if (importanceMap != null) {
            this.importanceMapDisplay = new ImageDisplay(importanceMap);
            this.importanceMapScrollPane = new JScrollPane(importanceMapDisplay);
            tabbedPane.addTab("Importance Map", importanceMapScrollPane);
        } else {
            this.importanceMapDisplay = null;
            this.importanceMapScrollPane = null;
        }

        add(tabbedPane, BorderLayout.CENTER);

        this.statusDisplay = new StatusDisplay(Orientation.Horizontal);

        add(statusDisplay, BorderLayout.PAGE_END);

        final Listener listener = new Listener(this);
        addComponentListener(listener);
        addWindowListener(listener);
    }

    public StatusDisplay getStatusDisplay() {
        return statusDisplay;
    }

    public synchronized void submit(VectorizerConfig config, Genome genome) {
        if (genome == null) {
            return;
        }

        currentGenome = genome;

        final long time = System.currentTimeMillis();
        if (time - lastRenderTime >= 1000) {
            lastRenderTime = time;
            statusDisplay.submit(config, genome);
            renderer.render(genome);
            currentImageDisplay.repaint();
        }
    }
    
    private void updateWindowTitle(String sessionName) {
        setTitle("Monalisa" + (sessionName != null && !sessionName.trim().isEmpty() ? " - " + sessionName.trim() : ""));
    }

    private JMenu buildFileMenu() {
        final JMenu menu = new JMenu("File");

        final JMenuItem renameSessionItem = new JMenuItem("Rename Session");
        renameSessionItem.addActionListener(new RenameSessionAction(this));
        
        final JMenuItem exitItem = new JMenuItem("Exit Application");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                monalisa.quit();
            }
        });

        final JMenuItem hideItem = new JMenuItem("Hide Window");
        hideItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        menu.add(renameSessionItem);
        menu.addSeparator();
        menu.add(hideItem);
        menu.add(exitItem);

        return menu;
    }

    private JMenu buildImageMenu() {
        final JMenu menu = new JMenu("Image");

        final JMenuItem exportClippedSVGItem = new JMenuItem("Export Clipped SVG...");
        exportClippedSVGItem.addActionListener(new ExportSVGAction(this, true));

        final JMenuItem exportSVGItem = new JMenuItem("Export SVG...");
        exportSVGItem.addActionListener(new ExportSVGAction(this, false));

        final JMenuItem exportTargetImageItem = new JMenuItem("Export Original Image...");
        exportTargetImageItem.addActionListener(new ExportTargetImageAction(this));

        menu.add(exportClippedSVGItem);
        menu.add(exportSVGItem);
        menu.addSeparator();
        menu.add(exportTargetImageItem);

        return menu;
    }
    
    private static class RenameSessionAction implements ActionListener {

        private final MainWindow window;
        
        private RenameSessionAction(MainWindow window) {
            this.window = Preconditions.checkNotNull(window, "The parameter 'window' must not be null");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            final String result = (String) JOptionPane.showInputDialog(
                    window, "Enter the new session name:", "Rename Session",
                    JOptionPane.PLAIN_MESSAGE, null, null,
                    window.sessionManager.getSessionName()
                    );
            if (result != null && !result.trim().isEmpty()) {
                try (final Database db = window.sessionManager.connect()) {
                    db.updateSetting("session-name", result.trim());
                    window.updateWindowTitle(result.trim());
                } catch (Exception e1) {
                    e1.printStackTrace();
                    JOptionPane.showConfirmDialog(window, "Unable to set session name!");
                }
            }
        }
    }

    private static class ExportTargetImageAction implements ActionListener {

        private final MainWindow window;

        private File checkFileName(File file) {
            if (file == null)
                return null;
            if (file.getName().toLowerCase().endsWith(".png")) {
                return file;
            }
            return new File(file.getParent(), file.getName() + ".png");
        }

        private ExportTargetImageAction(MainWindow window) {
            this.window = Preconditions.checkNotNull(window, "The parameter 'window' must not be null");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setDialogTitle("Export original image...");
            chooser.setSelectedFile(new File(window.sessionManager.getSessionName() + ".png"));
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
                }
                @Override
                public String getDescription() {
                    return "PNG Images";
                }
            });
            final int result = chooser.showDialog(window, "Export");
            if (result == JFileChooser.APPROVE_OPTION) {
                final File file = checkFileName(chooser.getSelectedFile());
                if (file != null) {
                    if (file.isFile()) {
                        int answer = JOptionPane.showOptionDialog(
                                window,
                                "The file '" + file.getName() + "' already exists!\n\nDo you want to replace that file?", 
                                "Replace file?", 
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
                                null, new String[] {"Cancel", "Replace"}, "Cancel");
                        if (answer == 0) {
                            return;
                        }
                    }
                    try {
                        window.sessionManager.exportTargetImage(file, false, true);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    private static class ExportSVGAction implements ActionListener {

        private final MainWindow window;
        private final boolean clipped;

        private File checkFileName(File file) {
            if (file == null)
                return null;
            if (file.getName().toLowerCase().endsWith(".svg")) {
                return file;
            }
            return new File(file.getParent(), file.getName() + ".svg");
        }

        private ExportSVGAction(MainWindow window, boolean clipped) {
            this.window = Preconditions.checkNotNull(window, "The parameter 'window' must not be null");
            this.clipped = clipped;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setDialogTitle("Export " + (clipped ? "clipped " : "") + "SVG image...");
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".svg");
                }
                @Override
                public String getDescription() {
                    return "SVG Images";
                }
            });
            final int result = chooser.showDialog(window, "Export");
            if (result == JFileChooser.APPROVE_OPTION) {
                final File file = checkFileName(chooser.getSelectedFile());
                if (file != null) {
                    if (file.isFile()) {
                        int answer = JOptionPane.showOptionDialog(
                                window,
                                "The file '" + file.getName() + "' already exists!\n\nDo you want to replace that file?", 
                                "Replace file?", 
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
                                null, new String[] {"Cancel", "Replace"}, "Cancel");
                        if (answer == 0) {
                            return;
                        }
                    }
                    try {
                        window.sessionManager.exportSVG(window.currentGenome, file, clipped, false, true);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
