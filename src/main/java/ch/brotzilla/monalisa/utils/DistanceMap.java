package ch.brotzilla.monalisa.utils;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.images.ImageData;

public class DistanceMap {

    private final ImageData targetImage;
    private final int blockSize;
    private final List<Block> blocks, blocksWrapper;
    private final int[] distances;
    private double maxDistance;
    
    private void allocateBlocks() {
        final int w = targetImage.getWidth(), h = targetImage.getHeight();
        int x = 0, y = 0;
        while (y < h) {
            final Block block = new Block(
                    x, y, 
                    Math.min(x + blockSize - 1, w - 1),
                    Math.min(y + blockSize - 1, h - 1)
                    );
            blocks.add(block);
            x += blockSize;
            if (x >= w) {
                x = 0;
                y += blockSize;
            }
        }
    }
    
    private void updateDistances(int[] image) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        Preconditions.checkArgument(image.length == targetImage.getLength(), "The length of the parameter 'image' has to be equal to " + targetImage.getLength());
        final int w = getWidth(), h = getHeight();
        final int[] target = targetImage.getBuffer();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int i = y * w + x;
                final int pt = target[i], pi = image[i];

                final int at = (pt >> 24) & 0x000000FF;
                final int rt = (pt >> 16) & 0x000000FF;
                final int gt = (pt >> 8) & 0x000000FF;
                final int bt = pt & 0x000000FF;

                final int ai = (pi >> 24) & 0x000000FF;
                final int ri = (pi >> 16) & 0x000000FF;
                final int gi = (pi >> 8) & 0x000000FF;
                final int bi = pi & 0x000000FF;

                final int ad = at - ai;
                final int rd = rt - ri;
                final int gd = gt - gi;
                final int bd = bt - bi;
                
                distances[i] = ad * ad + rd * rd + gd * gd + bd * bd;
            }
        }
    }
    
    public static class Block {
        
        public final int x1, y1, x2, y2, count;
        public double distance;
        
        public Block(int x1, int y1, int x2, int y2) {
            Preconditions.checkArgument(x1 >= 0, "The parameter 'x1' has to be greater than or equal to zero");
            Preconditions.checkArgument(y1 >= 0, "The parameter 'y1' has to be greater than or equal to zero");
            Preconditions.checkArgument(x2 >= x1, "The parameter 'x2' has to be greater than the parameter 'x1'");
            Preconditions.checkArgument(y2 >= y1, "The parameter 'y2' has to be greater than the parameter 'y1'");
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            count = (x2 - x1 + 1) * (y2 - y1 + 1);
        }
        
        public int getWidth() {
            return x2 - x1 + 1;
        }
        
        public int getHeight() {
            return y2 - y1 + 1;
        }
        
        @Override
        public String toString() {
            return "{x1 = " + x1 + ", y1 = " + y1 + ", x2 = " + x2 + ", y2 = " + y2 + ", count = " + count + ", width = " + getWidth() + ", height = " + getHeight() + "}";
        }
        
    }
    
    public DistanceMap(ImageData targetImage, int blockSize) {
        Preconditions.checkNotNull(targetImage, "The parameter 'targetImage' must not be null");
        Preconditions.checkArgument(blockSize > 0, "The parameter 'blockSize' has to be greater than zero");
        this.targetImage = targetImage;
        this.blockSize = blockSize;
        this.blocks = Lists.newArrayList();
        this.blocksWrapper = Collections.unmodifiableList(blocks);
        this.distances = new int[targetImage.getLength()];
        allocateBlocks();
    }
    
    public ImageData getTargetImage() {
        return targetImage;
    }
    
    public int getWidth() {
        return targetImage.getWidth();
    }
    
    public int getHeight() {
        return targetImage.getHeight();
    }
    
    public List<Block> getBlocks() {
        return blocksWrapper;
    }
    
    public double getMaxDistance() {
        return maxDistance;
    }
    
    public void update(int[] image) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        Preconditions.checkArgument(image.length == targetImage.getLength(), "The length of the parameter 'image' has to be equal to " + targetImage.getLength());
        updateDistances(image);
        final int w = getWidth();
        maxDistance = 0;
        for (final Block block : blocks) {
            double sum = 0;
            for (int y = block.y1; y <= block.y2; y++) {
                for (int x = block.x1; x <= block.x2; x++) {
                    sum += distances[y * w + x];
                }
            }
            final double distance = sum / block.count;
            block.distance = distance;
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
    }
    
}
