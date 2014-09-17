package ch.brotzilla.monalisa.evolution.fitness;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.evolution.intf.FitnessDecorator;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;

public class PerPolygonPunishment implements FitnessDecorator {

    private final double punishmentFactor;
    
    public PerPolygonPunishment(double punishmentFactor) {
        Preconditions.checkArgument(punishmentFactor >= 0, "The parameter 'punishmentFactor' has to be greater than or equal to zero");
        Preconditions.checkArgument(punishmentFactor < 1.0d, "The parameter 'punishmentFactor' has to be less than 1.0");
        this.punishmentFactor = punishmentFactor;
    }
    
    public double getPunishmentFactor() {
        return punishmentFactor;
    }

    @Override
    public double apply(VectorizerConfig config, Genome genome, double fitness) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        return fitness * punishmentFactor * genome.countPolygons();
    }

}
