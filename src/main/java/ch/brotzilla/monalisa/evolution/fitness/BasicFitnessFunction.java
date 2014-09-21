package ch.brotzilla.monalisa.evolution.fitness;

import java.text.DecimalFormat;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public class BasicFitnessFunction extends AbstractFitnessFunction {

    private final DecimalFormat ff = new DecimalFormat("#,###,###,###,##0.######");
    private final double alphaFactor, redFactor, greenFactor, blueFactor;
    
    public BasicFitnessFunction(double alphaFactor, double redFactor, double greenFactor, double blueFactor) {
        Preconditions.checkArgument(alphaFactor > 0, "The parameter 'alphaFactor' has to be greater than zero");
        Preconditions.checkArgument(redFactor > 0, "The parameter 'redFactor' has to be greater than zero");
        Preconditions.checkArgument(greenFactor > 0, "The parameter 'greenFactor' has to be greater than zero");
        Preconditions.checkArgument(blueFactor > 0, "The parameter 'blueFactor' has to be greater than zero");
        this.alphaFactor = alphaFactor;
        this.redFactor = redFactor;
        this.greenFactor = greenFactor;
        this.blueFactor = blueFactor;
    }
    
    public BasicFitnessFunction() {
        this(1.0d, 1.0d, 1.0d, 1.0d);
    }
    
    public double getAlphaFactor() {
        return alphaFactor;
    }

    public double getRedFactor() {
        return redFactor;
    }

    public double getGreenFactor() {
        return greenFactor;
    }

    public double getBlueFactor() {
        return blueFactor;
    }

    @Override
    public double compute(VectorizerConfig config, Genome genome, int[] inputData) {
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        Preconditions.checkNotNull(inputData, "The parameter 'inputData' must not be null");
        final int[] targetData = config.getVectorizerContext().getTargetImageData();
        final int[] importanceMap = config.getVectorizerContext().getImportanceMapData();
        double sum = 0;
        final int length = targetData.length;
        for (int i = 0; i < length; i++) {
            final int ic = inputData[i];
            final int ia = (ic >> 24) & 0x000000FF;
            final int ir = (ic >> 16) & 0x000000FF;
            final int ig = (ic >> 8) & 0x000000FF;
            final int ib = ic & 0x000000FF;
            final int tc = targetData[i];
            final int ta = (tc >> 24) & 0x000000FF;
            final int tr = (tc >> 16) & 0x000000FF;
            final int tg = (tc >> 8) & 0x000000FF;
            final int tb = tc & 0x000000FF;
            final int da = ia - ta;
            final int dr = ir - tr;
            final int dg = ig - tg;
            final int db = ib - tb;
            sum += ((da * da * alphaFactor) + (dr * dr * redFactor) + (dg * dg * greenFactor) + (db * db * blueFactor)) * (256 - importanceMap[i]);
        }
        return sum;
    }

    @Override
    public boolean isImprovement(Genome latest, Genome mutated) {
        Preconditions.checkNotNull(latest, "The parameter 'latest' must not be null");
        Preconditions.checkNotNull(mutated, "The parameter 'mutated' must not be null");
        return mutated.fitness < latest.fitness;
    }

    @Override
    public String format(double fitness) {
        return ff.format(fitness);
    }

}
