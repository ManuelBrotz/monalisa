package ch.brotzilla.monalisa.evolution.genes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.brotzilla.monalisa.utils.BoundingBox;
import ch.brotzilla.monalisa.utils.Utils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

public class Genome {

    public final Color background;
    public final Gene[][] genes;
    public double fitness;
    public int numberOfMutations, numberOfImprovements;
    public boolean overrideFitnessFlag;

    public Genome(Color background, Gene[][] genes, boolean copy) {
        Preconditions.checkNotNull(genes, "The parameter 'genes' must not be null");
        this.background = background;
        if (copy) {
            this.genes = Utils.copyGenes(genes);
        } else {
            this.genes = genes;
        }
    }
    
    public Genome(Color background, Gene[][] genes) {
        this(background, genes, true);
    }

    public Genome(Genome source) {
        this(Preconditions.checkNotNull(source, "The parameter 'source' must not be null").background, source.genes, true);
    }
    
    public Gene[] getCurrentLayer() {
        return genes[genes.length-1];
    }

    public void renderGenes(Graphics2D graphics) {
        Preconditions.checkNotNull(graphics, "The parameter 'graphics' must not be null");
        for (final Gene[] layer : genes) {
            for (final Gene gene : layer) {
                if (gene != null) {
                    gene.render(graphics);
                }
            }
        }
    }

    public int countPolygons() {
        int result = 0;
        for (final Gene[] layer : genes) {
            result += layer.length;
        }
        return result;
    }
    
    public int countPoints() {
        int result = 0;
        for (final Gene[] layer : genes) {
            for (final Gene gene : layer) {
                result += gene.x.length;
            }
        }
        return result;
    }
    
    public BoundingBox computeBoundingBox() {
        int xmin = Integer.MAX_VALUE, xmax = Integer.MIN_VALUE;
        int ymin = Integer.MAX_VALUE, ymax = Integer.MIN_VALUE;
        for (final Gene[] layer : genes) {
            for (Gene g : layer) {
                for (int i = 0; i < g.x.length; i++) {
                    final int x = g.x[i], y = g.y[i];
                    if (x < xmin) xmin = x;
                    if (x > xmax) xmax = x;
                    if (y < ymin) ymin = y;
                    if (y > ymax) ymax = y;
                }
            }
        }
        return new BoundingBox(xmin, ymin, xmax, ymax);
    }

    @Override
    public boolean equals(Object value) {
        if (value instanceof Genome) {
            final Genome v = (Genome) value;
            if (genes.length != v.genes.length || !Utils.equals(background, v.background) || !Utils.equals(fitness, v.fitness) || numberOfMutations != v.numberOfMutations || numberOfImprovements != v.numberOfImprovements)
                return false;
            final int length = genes.length;
            for (int i = 0; i < length; i++) {
                if (!Utils.equals(genes[i], v.genes[i]))
                    return false;
            }
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return toJson(this);
    }

    public static String toJson(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        return (new Gson()).toJson(genome);
    }

    public static Genome fromJson(String json) {
        return (new Gson()).fromJson(json, Genome.class);
    }
    
    public static Genome deserialize(DataInputStream in) throws IOException {
        Preconditions.checkNotNull(in, "The parameter 'in' must not be null");
        final byte version = in.readByte();
        Preconditions.checkState(version == 0 || version == 1, "Unable to deserialize genome, version not supported");
        final Color background = readColor(in);
        final double fitness = in.readDouble();
        final int numberOfImprovements = in.readInt();
        final int numberOfMutations = in.readInt();
        if (version == 0) {
            in.readInt(); // unused (for compatibility)
        }
        final int length = in.readInt();
        Preconditions.checkState(length > 0, "Unable to deserialize genome, too few " + (version == 0 ? "genomes" : "layers"));
        if (version == 0) {
            final Gene[] genes = new Gene[length];
            for (int i = 0; i < length; i++) {
                genes[i] = Gene.deserialize(in);
            }
            final Genome result = new Genome(background, new Gene[][] {genes}, false);
            result.fitness = fitness;
            result.numberOfImprovements = numberOfImprovements;
            result.numberOfMutations = numberOfMutations;
            return result;
        } else {
            Preconditions.checkArgument(version == 1, "Unable to deserialize genome, internal error");
            final Gene[][] genes = new Gene[length][];
            for (int i = 0; i < length; i++) {
                final int layerSize = in.readInt();
                Preconditions.checkArgument(layerSize > 0, "Unable to deserialize genome, invalid layer size");
                genes[i] = new Gene[layerSize];
                for (int j = 0; j < layerSize; j++) {
                    genes[i][j] = Gene.deserialize(in);
                }
            }
            final Genome result = new Genome(background, genes, false);
            result.fitness = fitness;
            result.numberOfImprovements = numberOfImprovements;
            result.numberOfMutations = numberOfMutations;
            return result;
        }
    }
    
    public static void serialize(Genome genome, DataOutputStream out) throws IOException {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        Preconditions.checkNotNull(out, "The parameter 'out' must not be null");
        out.writeByte(1); // version of serialization format
        writeColor(genome.background, out);
        out.writeDouble(genome.fitness);
        out.writeInt(genome.numberOfImprovements);
        out.writeInt(genome.numberOfMutations);
        out.writeInt(genome.genes.length);
        for (final Gene[] layer : genome.genes) {
            Preconditions.checkNotNull(layer, "Unable to serialize genome, null layers are not allowed");
            out.writeInt(layer.length);
            for (final Gene g : layer) {
                Gene.serialize(g, out);
            }
        }
    }
    
    private static Color readColor(DataInputStream in) throws IOException {
        final int color = in.readInt();
        if (color == 0)
            return null;
        return new Color(color, true);
    }
    
    private static void writeColor(Color color, DataOutputStream out) throws IOException {
        if (color == null) {
            out.writeInt(0);
        } else {
            out.writeInt(color.getRGB());
        }
    }
}
