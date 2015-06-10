package ch.brotzilla.monalisa.rendering;

import java.awt.Color;
import java.awt.Graphics2D;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.utils.ErrorMap;

public class ErrorMapRenderer extends Renderer {

    public ErrorMapRenderer(Image image) {
        super(image, false);
    }
    
    public ErrorMapRenderer(int width, int height) {
        super(width, height, false);
    }
    
    public void renderMap(ErrorMap map) {
        Preconditions.checkNotNull(map, "The parameter 'map' must not be null");
        Preconditions.checkArgument(map.getWidth() == getWidth(), "The width of the parameter 'map' has to be equal to " + getWidth());
        Preconditions.checkArgument(map.getHeight() == getHeight(), "The height of the parameter 'map' has to be equal to " + getHeight());
        final Graphics2D g = image.getGraphics();
        final double factor = 255 / map.getMaxError();
        for (final ErrorMap.Block block : map.getBlocks()) {
            final int value = Math.min(255, (int)(block.error * factor));
            final Color c = new Color(value, 0, 0);
            g.setColor(c);
            g.fillRect(block.x1, block.y1, block.w, block.h );
        }
    }

}
