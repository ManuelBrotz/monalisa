package ch.brotzilla.monalisa.gui;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.gui.StatusDisplay.Orientation;
import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.images.ImageType;
import ch.brotzilla.monalisa.io.SessionManager;
import ch.brotzilla.monalisa.rendering.SimpleRenderer;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
    
    protected final SessionManager sessionManager;
    protected final Image inputImage, currentImage;
    protected final Image importanceMap;
    protected final SimpleRenderer renderer;
    
    protected final JTabbedPane tabbedPane;
    protected final JScrollPane inputImageScrollPane, currentImageScrollPane, importanceMapScrollPane;
    protected final ImageDisplay inputImageDisplay, currentImageDisplay, importanceMapDisplay;

    protected final StatusDisplay statusDisplay;
    
    protected final StatisticsPanel statisticsPanel;
    
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
    
    public MainWindow(SessionManager sessionManager, Genome currentGenome) throws IOException { 
        super();
        
        this.sessionManager = Preconditions.checkNotNull(sessionManager, "The parameter 'sessionManager' must not be null");
        this.currentGenome = currentGenome;
        
        this.inputImage = new Image(sessionManager.getTargetImage());
        this.currentImage = new Image(ImageType.ARGB, sessionManager.getWidth(), sessionManager.getHeight());
        if (sessionManager.getImportanceMap() != null) {
            this.importanceMap = new Image(sessionManager.getImportanceMap());
        } else {
            this.importanceMap = null;
        }
        
        this.renderer = new SimpleRenderer(currentImage, false);
        if (currentGenome != null) {
            renderer.render(currentGenome);
        }
        
        setLayout(new BorderLayout());
        
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

        this.statisticsPanel = new StatisticsPanel(sessionManager);
        tabbedPane.addTab("Statistics", statisticsPanel);
        
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
    
    public synchronized void submit(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        
        currentGenome = genome;
        
        final long time = System.currentTimeMillis();
        if (time - lastRenderTime >= 1000) {
            lastRenderTime = time;
            statusDisplay.submit(genome);
            renderer.render(genome);
            currentImageDisplay.repaint();
        }
    }
}
