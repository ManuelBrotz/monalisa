package ch.brotzilla.monalisa.gui;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.Renderer;
import ch.brotzilla.monalisa.genes.Genome;
import ch.brotzilla.monalisa.utils.Constraints;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
    
    protected Constraints constraints;
    protected Renderer renderer;
    
    protected JTabbedPane tabbedPane;
    
    protected JScrollPane genomeScrollPane;
    protected GenomeDisplay genomeDisplay;

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
    
    public MainWindow(Constraints constraints) {
        
        this.constraints = Preconditions.checkNotNull(constraints, "The parameter 'constraints' must not be null");
        this.renderer = new Renderer(constraints.getWidth(), constraints.getHeight(), false);
        
        setLayout(new BorderLayout());
        
        this.tabbedPane = new JTabbedPane();
        
        this.genomeDisplay = new GenomeDisplay(renderer);
        this.genomeScrollPane = new JScrollPane(genomeDisplay);
        
        tabbedPane.addTab("Target", genomeScrollPane);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        final Listener listener = new Listener(this);
        addComponentListener(listener);
        addWindowListener(listener);
    }
    
    public void submit(Genome genome) {

    }
}
