package ch.brotzilla.monalisa.evolution.strategies;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.intf.IndexSelector;
import ch.brotzilla.monalisa.evolution.intf.RangeSelector;

public class EvolutionContext {

    private IndexSelector geneIndexSelector;
    private RangeSelector pointMutationRange, colorChannelMutationRange;
    protected int borderX, borderY;
    
    public EvolutionContext() {}

    public IndexSelector getGeneIndexSelector() {
        return geneIndexSelector;
    }
    
    public EvolutionContext setGeneIndexSelector(IndexSelector value) {
        Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
        this.geneIndexSelector = value;
        return this;
    }
    
    public RangeSelector getPointMutationRange() {
        return pointMutationRange;
    }
    
    public EvolutionContext setPointMutationRange(RangeSelector value) {
        Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
        this.pointMutationRange = value;
        return this;
    }
    
    public RangeSelector getColorChannelMutationRange() {
        return colorChannelMutationRange;
    }
    
    public EvolutionContext setColorChannelMutationRange(RangeSelector value) {
        Preconditions.checkNotNull(value, "The parameter 'value' must not be null");
        this.colorChannelMutationRange = value;
        return this;
    }
    
    public int getBorderX() {
        return borderX;
    }
    
    public EvolutionContext setBorderX(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.borderX = value;
        return this;
    }
    
    public int getBorderY() {
        return borderY;
    }
    
    public EvolutionContext setBorderY(int value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'value' has to be greater than or equal to zero");
        this.borderY = value;
        return this;
    }
    
    public EvolutionContext setBorder(int borderX, int borderY) {
        Preconditions.checkArgument(borderX >= 0 && borderY >= 0, "The parameters 'borderX' and 'borderY' have to be greater than or equal to zero");
        this.borderX = borderX;
        this.borderY = borderY;
        return this;
    }
    
}
