package ch.brotzilla.monalisa.mutations;

import ch.brotzilla.monalisa.mutations.intf.Mutation;

import com.google.common.base.Preconditions;

public abstract class BasicMutation implements Mutation {

    protected double probability;
    
    public BasicMutation() {}
    
    public BasicMutation(double probability) {
        Preconditions.checkArgument(probability >= 0, "The parameter 'probability' has to be in the range 0.0 - 1.0 inclusive");
        this.probability = probability;
    }
    
    @Override
    public double getProbability() {
        return probability;
    }

    @Override
    public void setProbability(double value) {
        Preconditions.checkArgument(value >= 0, "The parameter 'probability' has to be in the range 0.0 - 1.0 inclusive");
        this.probability = value;
    }
    
}