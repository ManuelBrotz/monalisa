package ch.brotzilla.monalisa.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.images.ImageData;
import ch.brotzilla.util.MersenneTwister;

public class ErrorMap {

    private final ImageData targetImage;
    private final int[] blockSizes;
    private final List<Block> blocks, blocksWrapper;
    private final int[] errors;
    private double averageError, averageError2, maxError;
    
    private void allocateBlocks() {
    	for (final int blockSize : blockSizes) {
    		allocateBlocks(blockSize, 0);
    		allocateBlocks(blockSize, blockSize / 2);
    	}
    }
    
    private void allocateBlocks(int blockSize, int delta) {
        final int w = targetImage.getWidth(), h = targetImage.getHeight();
        int x = -delta, y = -delta;
        while (y < h) {
            final Block block = new Block(
                    x < 0 ? 0 : x, 
                    y < 0 ? 0 : y, 
                    Math.min(x + blockSize - 1, w - 1),
                    Math.min(y + blockSize - 1, h - 1)
                    );
            blocks.add(block);
            x += blockSize;
            if (x >= w) {
                x = -delta;
                y += blockSize;
            }
        }
    }
    
    private void updateErrors(int[] image) {
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
                
                errors[i] = ad * ad + rd * rd + gd * gd + bd * bd;
            }
        }
    }
    
    private double computeAverageError2(double averageError) {
        int count = 0;
        double result = 0;
        for (final Block block : blocks) {
            if (block.error <= averageError) continue;
            count++;
            result += block.error;
        }
        return count > 0 ? result / count : 0;
    }
    
    public static class Block {
        
        public final int x1, y1, x2, y2, w, h, count;
        public double error;
        
        public Block(int x1, int y1, int x2, int y2) {
            Preconditions.checkArgument(x1 >= 0, "The parameter 'x1' has to be greater than or equal to zero");
            Preconditions.checkArgument(y1 >= 0, "The parameter 'y1' has to be greater than or equal to zero");
            Preconditions.checkArgument(x2 >= x1, "The parameter 'x2' has to be greater than the parameter 'x1'");
            Preconditions.checkArgument(y2 >= y1, "The parameter 'y2' has to be greater than the parameter 'y1'");
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            w = x2 - x1 + 1;
            h = y2 - y1 + 1;
            count = w * h;
        }
        
        @Override
        public String toString() {
            return "{x1 = " + x1 + ", y1 = " + y1 + ", x2 = " + x2 + ", y2 = " + y2 + ", distance = " + error + ", width = " + w + ", height = " + h + "}";
        }
        
    }
    
    public ErrorMap(ImageData targetImage, Integer... blockSizes) {
        Preconditions.checkNotNull(targetImage, "The parameter 'targetImage' must not be null");
        Preconditions.checkNotNull(blockSizes, "The parameter 'blockSizes' must not be null");
        for (int i = 0; i < blockSizes.length; i++) {
        	Preconditions.checkNotNull(blockSizes[i], "The parameter 'blockSizes[" + i + "] must not be null");
        	Preconditions.checkArgument(blockSizes[i] > 0, "The parameter 'blockSizes[" + i + "] has to be greater than zero");
        	Preconditions.checkArgument(blockSizes[i] % 2 == 0, "The parameter 'blockSizes[" + i + "] has to be even");
        }
        this.targetImage = targetImage;
        this.blockSizes = new int[blockSizes.length];
        for (int i = 0; i < blockSizes.length; i++) {
        	this.blockSizes[i] = blockSizes[i];
        }
        this.blocks = Lists.newArrayList();
        this.blocksWrapper = Collections.unmodifiableList(blocks);
        this.errors = new int[targetImage.getLength()];
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
    
    public int[] getBlockSizes() {
        final int[] result = new int[blockSizes.length];
        for (int i = 0; i < result.length; i++) {
        	result[i] = blockSizes[i];
        }
        return result;
    }
    
    public List<Block> getBlocks() {
        return blocksWrapper;
    }
    
    public int[] getErrors() {
        return errors;
    }
    
    public double getAverageError() {
        return averageError;
    }
    
    public double getAverageError2() {
        return averageError2;
    }
    
    public double getMaxError() {
        return maxError;
    }
    
    public void update(int[] image) {
        Preconditions.checkNotNull(image, "The parameter 'image' must not be null");
        Preconditions.checkArgument(image.length == targetImage.getLength(), "The length of the parameter 'image' has to be equal to " + targetImage.getLength());
        updateErrors(image);
        final int w = getWidth();
        averageError = 0;
        maxError = 0;
        for (final Block block : blocks) {
            double sum = 0;
            for (int y = block.y1; y <= block.y2; y++) {
                for (int x = block.x1; x <= block.x2; x++) {
                    sum += errors[y * w + x];
                }
            }
            final double error = sum / block.count;
            block.error = error;
            averageError += error;
            if (error > maxError) {
                maxError = error;
            }
        }
        averageError = blocks.size() > 0 ? averageError / blocks.size() : 0;
        averageError2 = computeAverageError2(averageError);
    }
    
    public Block[] selectRandomBlocks(int count, double minError, MersenneTwister rng) {
        Preconditions.checkArgument(count > 0, "The parameter 'count' has to be greater than zero");
        Preconditions.checkNotNull(rng, "The parameter 'rng' must not be null");
        final Block[] result = new Block[count];
        final Candidates candidates = selectCandidates(minError);
        if (candidates != null) {
            final double pointerSize = rng.nextDouble() / count;
            final double[] slots = candidates.slots;
            int slot = 0;
            for (int i = 0; i < count; i++) {
                final double pointer = pointerSize * (i + 1);
                while (slots[slot] <= pointer) {
                    ++slot;
                }
                result[i] = candidates.blocks[slot];
            }
            return result;
        }
        return null;
    }
    
    private Candidates selectCandidates(double minError) {
        Preconditions.checkArgument(minError >= 0, "The parameter 'minError' has to be greater than or equal to zero");
        final List<Block> result = Lists.newArrayList();
        double totalError = 0;
        for (final Block block : blocks) {
            if (block.error > minError) {
                result.add(block);
                totalError += block.error;
            }
        }
        if (result.size() > 0) {
            Collections.sort(result, new BlockComparator());
            return new Candidates(result, totalError);
        }
        return null;
    }
    
    private static class Candidates {
        
        public final Block[] blocks;
        public final double[] slots;
        
        public Candidates(List<Block> candidates, double totalError) {
            Preconditions.checkNotNull(candidates, "The parameter 'candidates' must not be null");
            Preconditions.checkArgument(candidates.size() > 0, "The parameter 'candidates' must not be empty");
            Preconditions.checkArgument(totalError > 0, "The parameter 'totalError' has to be greater than zero");
            final int size = candidates.size();
            this.blocks = new Block[size];
            this.slots = new double[size];
            for (int i = 0; i < size; i++) {
                final Block block = candidates.get(i);
                this.blocks[i] = block;
                this.slots[i] = block.error / totalError;
            }
        }
        
    }
    
    private static class BlockComparator implements Comparator<Block> {

        @Override
        public int compare(Block a, Block b) {
            Preconditions.checkNotNull(a, "The parameter 'a' must not be null");
            Preconditions.checkNotNull(b, "The parameter 'b' must not be null");
            if (a.error < b.error) {
                return -1;
            }
            if (a.error > b.error) {
                return 1;
            }
            return 0;
        }
        
    }
    
}
