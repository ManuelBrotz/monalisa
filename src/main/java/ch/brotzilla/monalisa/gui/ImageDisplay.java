package ch.brotzilla.monalisa.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.images.Image;

@SuppressWarnings("serial")
public class ImageDisplay extends JPanel {
    
    protected static final int TILE_SIZE = 8, TILE_COUNT = 10;
    protected static final Color COLOR_A = Color.GRAY, COLOR_B = Color.WHITE;
    protected static final BufferedImage BACKGROUND_IMAGE;
    protected static final TexturePaint BACKGROUND_PAINT;
    
    static {
        BACKGROUND_IMAGE = new BufferedImage(TILE_SIZE * TILE_COUNT, TILE_SIZE * TILE_COUNT, BufferedImage.TYPE_INT_ARGB);
        BACKGROUND_PAINT = new TexturePaint(BACKGROUND_IMAGE, new Rectangle(TILE_SIZE * TILE_COUNT, TILE_SIZE * TILE_COUNT));
        final Graphics2D g = BACKGROUND_IMAGE.createGraphics();
        for (int x = 0; x < TILE_COUNT; x++) {
            for (int y = 0; y < TILE_COUNT; y++) {
                final Color c = (x % 2 == 0) ? (y % 2 == 0 ? COLOR_A : COLOR_B) : (y % 2 == 0 ? COLOR_B : COLOR_A);
                g.setColor(c);
                g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    protected Image image;
    
    public ImageDisplay(Image image) {
        super();
        this.image = Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
    }
    
    @Override
    public void paint(Graphics g) {
        final Graphics2D g2 = (Graphics2D) g;
        final Paint p = g2.getPaint();
        
        try {
            g2.setPaint(BACKGROUND_PAINT);
            g2.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            g2.setPaint(p);
        }
        
        final int ww = getWidth(), wh = getHeight();
        final int iw = image.getWidth(), ih = image.getHeight();
        
        int x = 0, y = 0;
        if (ww > iw) {
            x = (ww - iw) / 2;
        }
        if (wh > ih) {
            y = (wh - ih) / 2;
        }
        
        g2.drawImage(image.getImage(), x, y, null);
    }
}
