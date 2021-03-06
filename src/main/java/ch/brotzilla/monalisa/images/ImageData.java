package ch.brotzilla.monalisa.images;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class ImageData {
    
    private final int width, height;
    private final ImageType type;
    private final int[] buffer;
    
    public ImageData(int width, int height, ImageType type, int[] data, boolean copy) {
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zero");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        Preconditions.checkNotNull(type, "The parameter 'type' must not be null");
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        Preconditions.checkArgument(data.length == width * height, "The length of the parameter 'data' must be equal to " + (width * height) + " (" + data.length + ")");
        this.width = width;
        this.height = height;
        this.type = type;
        if (copy) {
            this.buffer = new int[data.length];
            System.arraycopy(data, 0, this.buffer, 0, data.length);
        } else {
            this.buffer = data;
        }
    }
    
    public ImageData(int width, int height, byte[] data) {
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zero");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        Preconditions.checkArgument(data.length == width * height, "The length of the parameter 'data' must be equal to " + (width * height) + " (" + data.length + ")");
        this.width = width;
        this.height = height;
        this.type = ImageType.Gray;
        this.buffer = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            this.buffer[i] = data[i] & 0xFF;
        }
    }

    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public ImageType getType() {
        return type;
    }
    
    public int getLength() {
        return width * height;
    }
    
    public int[] getBuffer() {
        return buffer;
    }
    
    @Override
    public boolean equals(Object value) {
        if (value instanceof ImageData) {
            final ImageData v = (ImageData) value;
            final int[] a = buffer, b = v.buffer;
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
    
    @Override
    public String toString() {
        byte[] digest;
        try {
            digest = messageDigest(ImageData.this, "MD5");
        } catch (Exception e) {
            digest = null;
        }
        final StringBuilder b = new StringBuilder();
        b.append("{\"width\": " + width + ", \"height\": " + height + ", \"type\": \"" + type + "\", \"digest\": \"");
        if (digest != null) {
            for (int i = 0; i < digest.length; i++) {
                b.append(Strings.padStart(Integer.toHexString(digest[i] & 0xFF), 2, '0'));
            }
        } else {
            b.append("error");
        }
        b.append("\"}");
        return b.toString();
    }
    
    public static void serialize(ImageData data, DataOutputStream out) throws IOException {
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        Preconditions.checkNotNull(out, "The parameter 'out' must not be null");
        out.writeInt(data.getWidth());
        out.writeInt(data.getHeight());
        out.writeInt(data.getType().getBufferedImageType());
        out.writeInt(data.getLength());
        final int[] d = data.getBuffer();
        final int len = d.length;
        switch (data.getType()) {
        case ARGB:
        case ARGBPre:
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
            throw new IllegalArgumentException("Unable to serialize image data due to unsupported image type: " + data.getType());
        }
    }
    
    public static ImageData deserialize(DataInputStream in) throws IOException {
        Preconditions.checkNotNull(in, "The parameter 'in' must not be null");
        final int width = in.readInt();
        final int height = in.readInt();
        final int rawtype = in.readInt();
        final ImageType type = ImageType.fromBufferedImageType(rawtype);
        Preconditions.checkNotNull(type, "Unable to deserialize image data due to unknown image type (" + rawtype + ")");
        final int length = in.readInt();
        Preconditions.checkState(length == width * height, "Unable to deserialize image data due to invalid header");
        final int[] data = new int[length];
        switch (type) {
        case ARGB:
        case ARGBPre:
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
            throw new IllegalStateException("Unable to deserialize image data due to unknown image type: " + type);
        }
        return new ImageData(width, height, type, data, false);
    }
    
    public static ImageData createFrom(Image image, boolean readData) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        final int width = image.getWidth(), height = image.getHeight();
        switch (image.getType()) {
        case Gray: {
            return new ImageData(width, height, (byte[]) (readData ? image.readData() : image.getBuffer()));
        }
        case ARGB: 
        case ARGBPre: {
            return new ImageData(width, height, image.getType(), (int[]) (readData ? image.readData() : image.getBuffer()), true);
        }
        default:
            throw new IllegalArgumentException("Image type not supported (" + image.getType() + ")");
        }
    }
    
    public static ImageData createFrom(BufferedImage image) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        final int width = image.getWidth(), height = image.getHeight();
        switch (image.getType()) {
        case BufferedImage.TYPE_BYTE_GRAY: {
            final WritableRaster raster = image.getRaster();
            final byte[] data = (byte[]) raster.getDataElements(0, 0, width, height, null);
            return new ImageData(width, height, data);
        }
        case BufferedImage.TYPE_INT_ARGB: {
            final WritableRaster raster = image.getRaster();
            final int[] data = (int[]) raster.getDataElements(0, 0, width, height, null);
            return new ImageData(width, height, ImageType.ARGB, data, false);
        }
        case BufferedImage.TYPE_INT_ARGB_PRE: {
            final WritableRaster raster = image.getRaster();
            final int[] data = (int[]) raster.getDataElements(0, 0, width, height, null);
            return new ImageData(width, height, ImageType.ARGBPre, data, false);
        }
        default:
            throw new IllegalArgumentException("Image type not supported (" + image.getType() + ")");
        }
    }
    
    public static ImageData createFrom(BufferedImage image, ImageType convertTo) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        Preconditions.checkNotNull(convertTo, "The parameter 'convertTo' must not be null");
        final BufferedImage converted = convertTo.convertBufferedImage(image);
        return createFrom(converted);
    }

    public static BufferedImage createBufferedImage(ImageData data) {
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        final ImageType type = data.getType();
        final int width = data.getWidth(), height = data.getHeight();
        final int[] buffer = data.getBuffer();
        final BufferedImage image = type.createBufferedImage(width, height);
        final WritableRaster raster = image.getRaster();
        switch (type) {
        case ARGB:
        case ARGBPre:
            raster.setDataElements(0, 0, width, height, buffer);
            break;
        case Gray:
            final byte[] tmp = new byte[buffer.length];
            for (int i = 0; i < buffer.length; i++) {
                tmp[i] = (byte) buffer[i];
            }
            raster.setDataElements(0, 0, width, height, tmp);
            break;
        default:
            throw new IllegalArgumentException("Image type not supported (" + type + ")");
        }
        return image;
    }
    
    private static byte[] messageDigest(ImageData data, String algorithm) throws IOException, NoSuchAlgorithmException {
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        Preconditions.checkNotNull(algorithm, "The parameter 'algorithm' must not be null");
        final int[] d = data.buffer;
        final ByteArrayOutputStream bout;
        final DataOutputStream dout;
        switch (data.type) {
        case ARGB:
        case ARGBPre:
            bout = new ByteArrayOutputStream(d.length * 4);
            dout = new DataOutputStream(bout);
            for (int i = 0; i < d.length; i++) {
                dout.writeInt(d[i]);
            }
            break;
        case Gray:
            bout = new ByteArrayOutputStream(d.length);
            dout = new DataOutputStream(bout);
            for (int i = 0; i < d.length; i++) {
                dout.writeByte(d[i]);
            }
            break;
        default:
            throw new IllegalStateException("Unable to calculate md5 hash for image data due to unknown type: " + data.type);
        }
        final MessageDigest m = MessageDigest.getInstance(algorithm);
        return m.digest(bout.toByteArray());
    }
}
