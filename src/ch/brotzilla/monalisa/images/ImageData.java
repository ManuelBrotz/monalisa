package ch.brotzilla.monalisa.images;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.base.Preconditions;

public class ImageData {
    
    public enum Type {
        
        ARGB(BufferedImage.TYPE_INT_ARGB), Gray(BufferedImage.TYPE_BYTE_GRAY);
        
        private Type(int bufferedImageType) {
            this.bufferedImageType = bufferedImageType;
        }
        
        public final int bufferedImageType;
        
        public static Type fromBufferedImageType(int type) {
            switch (type) {
            case BufferedImage.TYPE_INT_ARGB:
                return ARGB;
            case BufferedImage.TYPE_BYTE_GRAY:
                return Gray;
            default:
                return null;
            }
        }
    }

    private final int width, height;
    private Type type;
    private final int[] data;
    
    public ImageData(int width, int height, Type type, int[] data) {
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zero");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        Preconditions.checkNotNull(type, "The parameter 'type' must not be null");
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        Preconditions.checkArgument(data.length == width * height, "The length of the parameter 'data' must be equal to " + (width * height) + " (" + data.length + ")");
        this.width = width;
        this.height = height;
        this.type = type;
        this.data = data;
    }
    
    public ImageData(int width, int height, byte[] data) {
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zero");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        Preconditions.checkArgument(data.length == width * height, "The length of the parameter 'data' must be equal to " + (width * height) + " (" + data.length + ")");
        this.width = width;
        this.height = height;
        this.type = Type.Gray;
        this.data = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i] & 0xFF;
        }
    }

    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public Type getType() {
        return type;
    }
    
    public int getLength() {
        return width * height;
    }
    
    public int[] getData() {
        return data;
    }
    
    @Override
    public boolean equals(Object value) {
        if (value != null && value instanceof ImageData) {
            final ImageData v = (ImageData) value;
            final int[] a = data, b = v.data;
            if (width == v.width && height == v.height && type == v.type && a.length == b.length) {
                for (int i = 0; i < a.length; i++) {
                    if (a[i] != b[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    public static void write(ImageData data, DataOutputStream out) throws IOException {
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        Preconditions.checkNotNull(out, "The parameter 'out' must not be null");
        out.writeInt(data.getWidth());
        out.writeInt(data.getHeight());
        out.writeInt(data.getType().bufferedImageType);
        out.writeInt(data.getLength());
        final int[] d = data.getData();
        final int len = d.length;
        switch (data.getType()) {
        case ARGB: 
            for (int i = 0; i < len; i++) {
                out.writeInt(d[i]);
            }
            break;
        case Gray:
            for (int i = 0; i < len; i++) {
                out.writeByte(d[i]);
            }
            break;
        default:
            throw new IllegalArgumentException("Unsupported image type: " + data.getType());
        }
    }
    
    public static ImageData read(DataInputStream in) throws IOException {
        Preconditions.checkNotNull(in, "The parameter 'in' must not be null");
        final int width = in.readInt();
        final int height = in.readInt();
        final Type type = Type.fromBufferedImageType(in.readInt());
        Preconditions.checkNotNull(type, "Cannot read image data due to unknown type");
        final int length = in.readInt();
        Preconditions.checkState(length == width * height, "Cannot read image data due to invalid header");
        final int[] data = new int[length];
        switch (type) {
        case ARGB: 
            for (int i = 0; i < length; i++) {
                data[i] = in.readInt();
            }
            break;
        case Gray:
            for (int i = 0; i < length; i++) {
                data[i] = in.readByte() & 0xFF;
            }
            break;
        default:
            throw new IllegalStateException("Unsupported image type: " + type);
        }
        return new ImageData(width, height, type, data);
    }
    
    public static ImageData read(BufferedImage image) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        final int width = image.getWidth(), height = image.getHeight();
        final WritableRaster raster = image.getRaster();
        switch (image.getType()) {
        case BufferedImage.TYPE_INT_ARGB: {
            final int[] data = (int[]) raster.getDataElements(0, 0, width, height, null);
            return new ImageData(width, height, Type.ARGB, data);
        }
        case BufferedImage.TYPE_BYTE_GRAY: {
            final byte[] data = (byte[]) raster.getDataElements(0, 0, width, height, null);
            return new ImageData(width, height, data);
        }
        default:
            throw new IllegalArgumentException("The parameter 'image' has to be of type BufferedImage.TYPE_INT_ARGB or BufferedImage.TYPE_BYTE_GRAY");
        }
    }
}
