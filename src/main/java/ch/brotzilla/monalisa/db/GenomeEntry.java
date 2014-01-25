package ch.brotzilla.monalisa.db;

import com.google.common.base.Preconditions;

public class GenomeEntry {

    private final int improvements;
    private final double fitness;
    private final byte[] data;

    public GenomeEntry(int improvements, double fitness, byte[] data) {
        Preconditions.checkArgument(improvements >= 0, "The parameter 'improvements' has to be greater than or equal to zero");
        Preconditions.checkNotNull(data, "The parameter 'data' must not be null");
        this.improvements = improvements;
        this.fitness = fitness;
        this.data = data;
    }

    public int getImprovements() {
        return improvements;
    }

    public double getFitness() {
        return fitness;
    }

    public byte[] getData() {
        return data;
    }
}