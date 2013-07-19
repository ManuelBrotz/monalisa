package ch.brotzilla.monalisa.genes;

import java.awt.Color;
import java.awt.Graphics2D;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

public class Genome {

    public final Color background;
    public final Gene[] genes;
    public double fitness;
    public long generated, selected;

    public Genome(Color background, Gene[] genes) {
        Preconditions.checkNotNull(genes, "The parameter 'genes' must not be null");
        this.background = background;
        this.genes = new Gene[genes.length];
        System.arraycopy(genes, 0, this.genes, 0, genes.length);
    }

    public Genome(Genome source) {
        this(Preconditions.checkNotNull(source, "The parameter 'source' must not be null").background, source.genes);
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
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append('[');
        if (background == null) {
            b.append("TRANSPARENT, ");
        } else {
            b.append(background.getAlpha() + ":" + background.getRed() + ":" + background.getGreen() + ":" + background.getBlue() + ", ");
        }
        for (final Gene gene : genes) {
            b.append(gene);
        }
        b.append(']');
        return b.toString();
    }

    public static String toJson(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        return (new Gson()).toJson(genome);
    }

    public static Genome fromJson(String json) {
        return (new Gson()).fromJson(json, Genome.class);
    }
}
