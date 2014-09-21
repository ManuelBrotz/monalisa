package ch.brotzilla.monalisa.evolution.fitness;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.FitnessDecorator;
import ch.brotzilla.monalisa.evolution.intf.FitnessFunction;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public class DecoratedFitnessFunction extends AbstractFitnessFunction {

    private final FitnessFunction delegate;
    private final List<FitnessDecorator> decorators;
    
    public DecoratedFitnessFunction(FitnessFunction delegate) {
        Preconditions.checkNotNull(delegate, "The parameter 'delegate' must not be null");
        this.delegate = delegate;
        this.decorators = Lists.newArrayList();
    }
    
    public FitnessFunction getDelegate() {
        return delegate;
    }
    
    public List<FitnessDecorator> getDecorators() {
        return decorators;
    }

    @Override
    public double compute(VectorizerConfig config, Genome genome, int[] inputData) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        double result = delegate.compute(config, genome, inputData);
        double decoration = 0;
        for (final FitnessDecorator d : decorators) {
            if (d == null) {
                continue;
            }
            decoration += d.apply(config, genome, result);
        }
        return result + decoration;
    }

    @Override
    public boolean isImprovement(Genome latest, Genome mutated) {
        return delegate.isImprovement(latest, mutated);
    }

    @Override
    public String format(double fitness) {
        return delegate.format(fitness);
    }

}
