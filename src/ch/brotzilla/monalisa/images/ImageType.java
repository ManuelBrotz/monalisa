package ch.brotzilla.monalisa.images;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.google.common.base.Preconditions;

public enum ImageType {
    
    ARGB(BufferedImage.TYPE_INT_ARGB) {
        @Override
        public Object createArray(int length) {
            return new int[length];
        }
    },
    ARGBPre(BufferedImage.TYPE_INT_ARGB_PRE) {
        @Override
        public Object createArray(int length) {
            return new int[length];
        }
    },
    Gray(BufferedImage.TYPE_BYTE_GRAY) {
        @Override
        public Object createArray(int length) {
            return new byte[length];
        }
    };
    
    private ImageType(int bufferedImageType) {
        this.bufferedImageType = bufferedImageType;
    }
    
    private final int bufferedImageType;
    
    public int getBufferedImageType() {
        return bufferedImageType;
    }
    
    public abstract Object createArray(int length);
    
    public BufferedImage createBufferedImage(int width, int height) {
        return new BufferedImage(width, height, getBufferedImageType());
    }

    public BufferedImage convertBufferedImage(BufferedImage input) {
        Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
        if (input.getType() == getBufferedImageType()) {
            return input;
        }
        final int width = input.getWidth(), height = input.getHeight();
        final BufferedImage result = createBufferedImage(width, height);
        final Graphics2D g = result.createGraphics();
        g.drawImage(input, 0, 0, null);
        return result;
    }
    
    public static ImageType fromBufferedImageType(int type) {
        switch (type) {
        case BufferedImage.TYPE_INT_ARGB:
            return ARGB;
        case BufferedImage.TYPE_INT_ARGB_PRE:
            return ARGBPre;
        case BufferedImage.TYPE_BYTE_GRAY:
            return Gray;
        default:
            return null;
        }
    }

    public static boolean isSupported(int type) {
        return fromBufferedImageType(type) != null;
    }
    
    public static void check(int type) {
        if (fromBufferedImageType(type) == null) {
            throw new IllegalArgumentException("Image type not supported (" + type + ")");
        }
    }
}
