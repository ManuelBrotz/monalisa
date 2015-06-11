package ch.brotzilla.monalisa.rendering;

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
        final double factor = 255 / map.getMaxError();
        final int w = getWidth();
        final int[] buffer = image.getBuffer();
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = 0xFF000000;
        }
        for (final ErrorMap.Block block : map.getBlocks()) {
            if (block.error <= map.getAverageError2()) continue;
            final int value = Math.min(255, (int) (block.error * factor));
            for (int y = block.y1; y <= block.y2; y++) {
                for (int x = block.x1; x <= block.x2; x++) {
                    final int i = y * w + x;
                    final int bufferValue = (buffer[i] >> 16) & 0x000000FF;
                    if (bufferValue < value) {
                        buffer[i] = 0xFF000000 | value << 16;
                    }
                }
            }
        }
        image.writeData();
    }

}
