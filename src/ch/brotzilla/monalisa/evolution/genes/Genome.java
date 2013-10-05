package ch.brotzilla.monalisa.evolution.genes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.brotzilla.monalisa.utils.Utils;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

public class Genome {

    public final Color background;
    public final Gene[] genes;
    public double fitness;
    public int generated, selected, mutations;

    public Genome(Color background, Gene[] genes, boolean copy) {
        Preconditions.checkNotNull(genes, "The parameter 'genes' must not be null");
        this.background = background;
        if (copy) {
            this.genes = new Gene[genes.length];
            System.arraycopy(genes, 0, this.genes, 0, genes.length);
        } else {
            this.genes = genes;
        }
    }
    
    public Genome(Color background, Gene[] genes) {
        this(background, genes, true);
    }

    public Genome(Genome source) {
        this(Preconditions.checkNotNull(source, "The parameter 'source' must not be null").background, source.genes, true);
    }

    public void renderGenes(Graphics2D graphics) {
        Preconditions.checkNotNull(graphics, "The parameter 'graphics' must not be null");
        for (final Gene gene : genes) {
            if (gene != null) {
                gene.render(graphics);
            }
        }
    }

    public int countPoints() {
        int result = 0;
        for (final Gene gene : genes) {
            result += gene.x.length;
        }
        return result;
    }

    @Override
    public boolean equals(Object value) {
        if (value instanceof Genome) {
            final Genome v = (Genome) value;
            if (genes.length != v.genes.length || !Utils.equals(background, v.background) || !Utils.equals(fitness, v.fitness) || generated != v.generated || selected != v.selected || mutations != v.mutations)
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
    
    public static Genome read(DataInputStream in) throws IOException {
        Preconditions.checkNotNull(in, "The parameter 'in' must not be null");
        final byte version = in.readByte();
        Preconditions.checkState(version == 0, "Unable to deserialize genome, version not supported");
        final Color background = readColor(in);
        final double fitness = in.readDouble();
        final int selected = in.readInt();
        final int generated = in.readInt();
        final int mutations = in.readInt();
        final int length = in.readInt();
        Preconditions.checkState(length > 0, "Unable to deserialize genome, too few genes");
        final Gene[] genes = new Gene[length];
        for (int i = 0; i < length; i++) {
            genes[i] = Gene.read(in);
        }
        final Genome result = new Genome(background, genes, false);
        result.fitness = fitness;
        result.selected = selected;
        result.generated = generated;
        result.mutations = mutations;
        return result;
    }
    
    public static void write(Genome genome, DataOutputStream out) throws IOException {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        Preconditions.checkNotNull(out, "The parameter 'out' must not be null");
        out.writeByte(0); // version of serialization format
        writeColor(genome.background, out);
        out.writeDouble(genome.fitness);
        out.writeInt(genome.selected);
        out.writeInt(genome.generated);
        out.writeInt(genome.mutations);
        out.writeInt(genome.genes.length);
        for (final Gene g : genome.genes) {
            Gene.write(g, out);
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
