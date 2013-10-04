package ch.brotzilla.monalisa.images;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import com.google.common.base.Preconditions;

public class ImageGray extends Image {

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
    protected void internalReadData(WritableRaster raster) {
        Preconditions.checkNotNull(raster, "The parameter 'raster' must not be null");
        Preconditions.checkNotNull(data, "The internal field 'data' must not be null");
        raster.getDataElements(0, 0, width, height, data);
    }

    public ImageGray(BufferedImage image, boolean readData) {
        super(image, readData);
        this.data = createData();
    }

    public ImageGray(int width, int height, boolean readData) {
        super(width, height, readData);
        this.data = createData();
    }

}
