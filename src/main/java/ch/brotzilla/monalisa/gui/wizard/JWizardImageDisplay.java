package ch.brotzilla.monalisa.gui.wizard;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

@SuppressWarnings("serial")
class JWizardImageDisplay extends JPanel {
    
    private Image image;
    
    public JWizardImageDisplay() {
        
    }
    
    public Image getImage() {
        return image;
    }
    
    public void setImage(Image image) {
        this.image = image;
        repaint();
    }
    
    @Override
    public void paint(Graphics g) {
        if (image == null) {
            super.paint(g);
        } else {
            final int w = image.getWidth(null), h = image.getHeight(null);
            final double cx = getWidth() / 2.0d, cy = getHeight() / 2.0d; 
            g.drawImage(image, 
                    (int) Math.round(cx - w / 2.0d), 
                    (int) Math.round(cy - h / 2.0d), 
                    w, h, null);
        }
    }
    
}
