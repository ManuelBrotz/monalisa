package ch.brotzilla.monalisa.images;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import ch.brotzilla.monalisa.images.ImageData.Type;

import com.google.common.base.Preconditions;

public class ImageGray extends Image<byte[]> {

    public final byte[] data;
    
    protected byte[] createData() {
        if (readData) {
            return new byte[size];
        }
        return null;
    }
    
    @Override
    protected int calculateLengthInBytes(int size) {
        return size;
    }

    @Override
    protected BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    }

    @Override
    protected BufferedImage checkImage(BufferedImage image) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        Preconditions.checkArgument(image.getType() == BufferedImage.TYPE_BYTE_GRAY, "The type of the parameter 'image' must be BufferedImage.TYPE_BYTE_GRAY");
        return image;
    }

    @Override
    protected BufferedImage convertImageData(ImageData image) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        Preconditions.checkArgument(image.getType() == Type.Gray, "The type of the parameter 'image' must be BufferedImage.TYPE_BYTE_GRAY");
        final int[] input = image.getData();
        final int length = image.getLength();
        final byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) input[i];
        }
        final BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        final WritableRaster raster = img.getRaster();
        raster.setDataElements(0, 0, image.getWidth(), image.getHeight(), data);
        return img;
    }

    @Override
    protected byte[] internalReadData(WritableRaster raster) {
        Preconditions.checkNotNull(raster, "The parameter 'raster' must not be null");
        Preconditions.checkNotNull(data, "The internal field 'data' must not be null");
        raster.getDataElements(0, 0, width, height, data);
        return data;
    }

    public ImageGray(BufferedImage image, boolean readData) {
        super(image, readData);
        this.data = createData();
    }

    public ImageGray(ImageData image, boolean readData) {
        super(image, readData);
        this.data = createData();
    }

    public ImageGray(int width, int height, boolean readData) {
        super(width, height, readData);
        this.data = createData();
    }
}
