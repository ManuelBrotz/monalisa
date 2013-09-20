package ch.brotzilla.monalisa.gui;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.Renderer;
import ch.brotzilla.monalisa.genes.Genome;

@SuppressWarnings("serial")
public class GenomeDisplay extends JPanel {

    protected Renderer renderer;
    
    public GenomeDisplay(Renderer renderer) {
        this.renderer = Preconditions.checkNotNull(renderer, "The parameter 'renderer' must not be null");
        setPreferredSize(new Dimension(renderer.width, renderer.height));
    }
    
    public void render(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        renderer.render(genome);
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(renderer.getImage().image, 0, 0, null);
    }
}
