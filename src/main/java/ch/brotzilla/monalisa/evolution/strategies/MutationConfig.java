package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.intf.IndexSelector;
import ch.brotzilla.monalisa.evolution.intf.RangeSelector;

public class MutationConfig {

    private final IndexSelector geneIndexSelector;
    private final RangeSelector pointMutationRange, colorChannelMutationRange;
    private final int outerBorderX, outerBorderY, innerBorderX, innerBorderY;
    private final double geneVersusGenomeMutationProbability;
    private final int minMutationsPerGenome, maxMutationsPerGenome;

    private MutationConfig(Builder builder) {
        Preconditions.checkNotNull(builder, "The parameter 'builder' must not be null");
        builder.checkReady();
        this.geneIndexSelector = builder.getGeneIndexSelector();
        this.pointMutationRange = builder.getPointMutationRange();
        this.colorChannelMutationRange = builder.getColorChannelMutationRange();
        this.outerBorderX = builder.getOuterBorderX();
        this.outerBorderY = builder.getOuterBorderY();
        this.innerBorderX = builder.getInnerBorderX();
        this.innerBorderY = builder.getInnerBorderY();
        this.geneVersusGenomeMutationProbability = builder.getGeneVersusGenomeMutationProbability();
        this.minMutationsPerGenome = builder.getMinMutationsPerGenome();
        this.maxMutationsPerGenome = builder.getMaxMutationsPerGenome();
    }

    public IndexSelector getGeneIndexSelector() {
        return geneIndexSelector;
    }

    public RangeSelector getPointMutationRange() {
        return pointMutationRange;
    }

    public RangeSelector getColorChannelMutationRange() {
        return colorChannelMutationRange;
    }

    public int getOuterBorderX() {
        return outerBorderX;
    }

    public int getOuterBorderY() {
        return outerBorderY;
    }

    public int getInnerBorderX() {
        return innerBorderX;
    }

    public int getInnerBorderY() {
        return innerBorderY;
    }

    public double getGeneVersusGenomeMutationProbability() {
        return geneVersusGenomeMutationProbability;
    }
    
    public int getMinMutationsPerGenome() {
        return minMutationsPerGenome;
    }
    
    public int getMaxMutationsPerGenome() {
        return maxMutationsPerGenome;
    }
    
    public static class Builder {

        private IndexSelector geneIndexSelector;
        private RangeSelector pointMutationRange, colorChannelMutationRange;
        private int outerBorderX, outerBorderY, innerBorderX, innerBorderY;
        private double geneVersusGenomeMutationProbability;
        private int minMutationsPerGenome, maxMutationsPerGenome;

        private void checkReady() {
            Preconditions.checkNotNull(getGeneIndexSelector(), "The property 'GeneIndexSelector' must not be null");
            Preconditions.checkNotNull(getPointMutationRange(), "The property 'PointMutationRange' must not be null");
            Preconditions.checkNotNull(getColorChannelMutationRange(), "The property 'ColorChannelMutationRange' must not be null");
            Preconditions.checkState(getOuterBorderX() >= 0, "The property 'OuterBorderX' has to be greater than or equal to zero");
            Preconditions.checkState(getOuterBorderY() >= 0, "The property 'OuterBorderY' has to be greater than or equal to zero");
            Preconditions.checkState(getInnerBorderX() >= 0, "The property 'InnerBorderX' has to be greater than or equal to zero");
            Preconditions.checkState(getInnerBorderY() >= 0, "The property 'InnerBorderY' has to be greater than or equal to zero");
            Preconditions.checkState(getGeneVersusGenomeMutationProbability() >= 0.0d, "The property 'GeneVersusGenomeMutationProbability' has to be greater than or equal to zero");
            Preconditions.checkState(getMinMutationsPerGenome() > 0, "The property 'MinMutationsPerGenome' has to be greater than zero");
            Preconditions.checkState(getMaxMutationsPerGenome() > getMinMutationsPerGenome(), "The property 'MaxMutationsPerGenome' has to be greater than the property 'MinMutationsPerGenome'");
            Preconditions.checkState(isReady(), "The mutation configuration is not ready");
        }
        
        public Builder() {
        }
        
        public boolean isReady() {
            return geneIndexSelector != null 
                    && pointMutationRange != null
                    && colorChannelMutationRange != null
                    && outerBorderX >= 0
                    && outerBorderY >= 0
                    && innerBorderX >= 0
                    && innerBorderY >= 0
                    && geneVersusGenomeMutationProbability >= 0.0d
                    && minMutationsPerGenome > 0
                    && maxMutationsPerGenome > minMutationsPerGenome;
        }

        public IndexSelector getGeneIndexSelector() {
            return geneIndexSelector;
        }

        public Builder setGeneIndexSelector(IndexSelector value) {
            Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
            this.geneIndexSelector = value;
            return this;
        }

        public RangeSelector getPointMutationRange() {
            return pointMutationRange;
        }

        public Builder setPointMutationRange(RangeSelector value) {
            Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
            this.pointMutationRange = value;
            return this;
        }

        public RangeSelector getColorChannelMutationRange() {
            return colorChannelMutationRange;
        }

        public Builder setColorChannelMutationRange(RangeSelector value) {
            Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
            this.colorChannelMutationRange = value;
            return this;
        }

        public int getOuterBorderX() {
            return outerBorderX;
        }

        public Builder setOuterBorderX(int value) {
            Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
            this.outerBorderX = value;
            return this;
        }

        public int getOuterBorderY() {
            return outerBorderY;
        }

        public Builder setOuterBorderY(int value) {
            Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
            this.outerBorderY = value;
            return this;
        }

        public Builder setOuterBorder(int borderX, int borderY) {
            Preconditions.checkArgument(borderX >= 0 && borderY >= 0, "The parameters 'borderX' and 'borderY' have to be greater than or equal to zero");
            this.outerBorderX = borderX;
            this.outerBorderY = borderY;
            return this;
        }

        public int getInnerBorderX() {
            return innerBorderX;
        }

        public Builder setInnerBorderX(int value) {
            Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
            this.innerBorderX = value;
            return this;
        }

        public int getInnerBorderY() {
            return innerBorderY;
        }

        public Builder setInnerBorderY(int value) {
            Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
            this.innerBorderY = value;
            return this;
        }

        public Builder setInnerBorder(int borderX, int borderY) {
            Preconditions.checkArgument(borderX >= 0 && borderY >= 0, "The parameters 'borderX' and 'borderY' have to be greater than or equal to zero");
            this.innerBorderX = borderX;
            this.innerBorderY = borderY;
            return this;
        }

        public double getGeneVersusGenomeMutationProbability() {
            return geneVersusGenomeMutationProbability;
        }
        
        public Builder setGeneVersusGenomeMutationProbability(double value) {
            Preconditions.checkArgument(value > 0.0d, "The parameter 'value' has to be greater than zero");
            this.geneVersusGenomeMutationProbability = value;
            return this;
        }
        
        
        public int getMinMutationsPerGenome() {
            return minMutationsPerGenome;
        }
        
        public Builder setMinMutationsPerGenome(int value) {
            Preconditions.checkArgument(value >= 1, "The parameter 'value' has to be greater than 1");
            this.minMutationsPerGenome = value;
            return this;
        }
        
        public int getMaxMutationsPerGenome() {
            return maxMutationsPerGenome;
        }
        
        public Builder setMaxMutationsPerGenome(int value) {
            Preconditions.checkArgument(value >= 1, "The parameter 'value' has to be greater than 1");
            this.maxMutationsPerGenome = value;
            return this;
        }

        public MutationConfig build() {
            return new MutationConfig(this);
        }
        
    }
    
}
