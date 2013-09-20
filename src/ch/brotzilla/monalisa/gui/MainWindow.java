package ch.brotzilla.monalisa.gui;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.Renderer;
import ch.brotzilla.monalisa.genes.Genome;
import ch.brotzilla.monalisa.images.ImageARGB;
import ch.brotzilla.monalisa.images.ImageGray;
import ch.brotzilla.monalisa.utils.SessionManager;
import ch.brotzilla.monalisa.utils.Utils;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
    
    protected final SessionManager sessionManager;
    protected final ImageARGB inputImage, currentImage;
    protected final ImageGray importanceMap;
    protected final Renderer renderer;
    
    protected final JTabbedPane tabbedPane;
    protected final JScrollPane inputImageScrollPane, currentImageScrollPane, importanceMapScrollPane;
    protected final ImageDisplay inputImageDisplay, currentImageDisplay, importanceMapDisplay;

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
            System.out.println("minimized");
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
        
        this.sessionManager = Preconditions.checkNotNull(sessionManager, "The parameter 'sessionManager' must not be null");
        this.currentGenome = currentGenome;
        
        this.inputImage = new ImageARGB(Utils.readImage(sessionManager.getInputImageFile()), false);
        this.currentImage = new ImageARGB(sessionManager.getWidth(), sessionManager.getHeight(), false);
        if (sessionManager.getImportanceMapFile().isFile()) {
            this.importanceMap = new ImageGray(ImageIO.read(sessionManager.getImportanceMapFile()), false);
        } else {
            this.importanceMap = null;
        }
        
        this.renderer = new Renderer(currentImage);
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
        
        add(tabbedPane, BorderLayout.CENTER);
        
        final Listener listener = new Listener(this);
        addComponentListener(listener);
        addWindowListener(listener);
    }
    
    public void submit(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        currentGenome = genome;
        final long time = System.currentTimeMillis();
        if (time - lastRenderTime >= 1000) {
            lastRenderTime = time;
            renderer.render(genome);
            currentImageDisplay.repaint();
        }
    }
}
