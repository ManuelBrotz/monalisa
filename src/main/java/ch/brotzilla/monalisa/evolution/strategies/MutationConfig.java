package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.intf.IndexSelector;
import ch.brotzilla.monalisa.evolution.intf.RangeSelector;

public class MutationConfig {

    private IndexSelector geneIndexSelector;
    private RangeSelector pointMutationRange, colorChannelMutationRange;
    protected int outerBorderX, outerBorderY, innerBorderX, innerBorderY;

    public MutationConfig() {
    }

    public IndexSelector getGeneIndexSelector() {
        return geneIndexSelector;
    }

    public MutationConfig setGeneIndexSelector(IndexSelector value) {
        Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
        this.geneIndexSelector = value;
        return this;
    }

    public RangeSelector getPointMutationRange() {
        return pointMutationRange;
    }

    public MutationConfig setPointMutationRange(RangeSelector value) {
        Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
        this.pointMutationRange = value;
        return this;
    }

    public RangeSelector getColorChannelMutationRange() {
        return colorChannelMutationRange;
    }

    public MutationConfig setColorChannelMutationRange(RangeSelector value) {
        Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
        this.colorChannelMutationRange = value;
        return this;
    }

    public int getOuterBorderX() {
        return outerBorderX;
    }

    public MutationConfig setOuterBorderX(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.outerBorderX = value;
        return this;
    }

    public int getOuterBorderY() {
        return outerBorderY;
    }

    public MutationConfig setOuterBorderY(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.outerBorderY = value;
        return this;
    }

    public MutationConfig setOuterBorder(int borderX, int borderY) {
        Preconditions.checkArgument(borderX >= 0 && borderY >= 0, "The parameters 'borderX' and 'borderY' have to be greater than or equal to zero");
        this.outerBorderX = borderX;
        this.outerBorderY = borderY;
        return this;
    }

    public int getInnerBorderX() {
        return innerBorderX;
    }

    public MutationConfig setInnerBorderX(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.innerBorderX = value;
        return this;
    }

    public int getInnerBorderY() {
        return innerBorderY;
    }

    public MutationConfig setInnerBorderY(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.innerBorderY = value;
        return this;
    }

    public MutationConfig setInnerBorder(int borderX, int borderY) {
        Preconditions.checkArgument(borderX >= 0 && borderY >= 0, "The parameters 'borderX' and 'borderY' have to be greater than or equal to zero");
        this.innerBorderX = borderX;
        this.innerBorderY = borderY;
        return this;
    }
    
}
