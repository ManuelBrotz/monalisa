package ch.brotzilla.monalisa.rendering;

import java.awt.Color;
import java.awt.Graphics2D;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.utils.DistanceMap;

public class DistanceMapRenderer extends Renderer {

    public DistanceMapRenderer(Image image, boolean autoUpdateBuffer) {
        super(image, autoUpdateBuffer);
    }
    
    public DistanceMapRenderer(int width, int height, boolean autoUpdateBuffer) {
        super(width, height, autoUpdateBuffer);
    }
    
    public void renderMap(DistanceMap map) {
        Preconditions.checkNotNull(map, "The parameter 'map' must not be null");
        Preconditions.checkArgument(map.getWidth() == getWidth(), "The width of the parameter 'map' has to be equal to " + getWidth());
        Preconditions.checkArgument(map.getHeight() == getHeight(), "The height of the parameter 'map' has to be equal to " + getHeight());
        final Graphics2D g = image.getGraphics();
        final double factor = 255 / map.getMaxDistance();
        for (final DistanceMap.Block block : map.getBlocks()) {
            final int value = 255 - Math.min(255, (int)(block.distance * factor));
            final Color c = new Color(value, 0, 0);
            g.setColor(c);
            g.fillRect(block.x1, block.y1, block.getWidth(), block.getHeight());
        }
    }

}
