package ch.brotzilla.monalisa.evolution.genes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.brotzilla.monalisa.utils.Utils;

import com.google.common.base.Preconditions;

public class Gene {

    public final int[] x, y, color;

    public Gene(int[] x, int[] y, int[] color, boolean copy) {
        Preconditions.checkNotNull(x, "The parameter 'x' must not be null");
        Preconditions.checkArgument(x.length >= 3, "The parameter 'x' must be of length 3 or greater");
        Preconditions.checkNotNull(y, "The parameter 'y' must not be null");
        Preconditions.checkArgument(y.length >= 3, "The parameter 'y' must be of length 3 or greater");
        Preconditions.checkArgument(x.length == y.length, "The parameters 'x' and 'y' must be of equal length");
        Preconditions.checkNotNull(color, "The parameter 'color' must not be null");
        Preconditions.checkArgument(color.length == 4, "The parameter 'color' must be of length 4");
        if (copy) {
            this.x = new int[x.length];
            System.arraycopy(x, 0, this.x, 0, x.length);
            this.y = new int[y.length];
            System.arraycopy(y, 0, this.y, 0, y.length);
            this.color = new int[] { color[0], color[1], color[2], color[3] };
        } else {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
    
    public Gene(int[] x, int[] y, int[] color) {
        this(x, y, color, true);
    }

    public Gene(int[] x, int[] y, Color color) {
        Preconditions.checkNotNull(x, "The parameter 'x' must not be null");
        Preconditions.checkArgument(x.length >= 3, "The parameter 'x' must be of length 3 or greater");
        Preconditions.checkNotNull(y, "The parameter 'y' must not be null");
        Preconditions.checkArgument(y.length >= 3, "The parameter 'y' must be of length 3 or greater");
        Preconditions.checkArgument(x.length == y.length, "The parameters 'x' and 'y' must be of equal length");
        Preconditions.checkNotNull(color, "The parameter 'color' must not be null");
        this.x = new int[x.length];
        System.arraycopy(x, 0, this.x, 0, x.length);
        this.y = new int[y.length];
        System.arraycopy(y, 0, this.y, 0, y.length);
        this.color = new int[] { color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue() };
    }

    public Gene(int[] x, int[] y, int color) {
        Preconditions.checkNotNull(x, "The parameter 'x' must not be null");
        Preconditions.checkArgument(x.length >= 3, "The parameter 'x' must be of length 3 or greater");
        Preconditions.checkNotNull(y, "The parameter 'y' must not be null");
        Preconditions.checkArgument(y.length >= 3, "The parameter 'y' must be of length 3 or greater");
        Preconditions.checkArgument(x.length == y.length, "The parameters 'x' and 'y' must be of equal length");
        this.x = new int[x.length];
        System.arraycopy(x, 0, this.x, 0, x.length);
        this.y = new int[y.length];
        System.arraycopy(y, 0, this.y, 0, y.length);
        final Color tmp = new Color(color, true);
        this.color = new int[] { tmp.getAlpha(), tmp.getRed(), tmp.getGreen(), tmp.getBlue() };
    }

    public Gene(Gene coords, int color) {
        this(Preconditions.checkNotNull(coords, "The parameter 'coords' must not be null").x, coords.y, color);
    }

    public Gene(Gene coords, Color color) {
        this(Preconditions.checkNotNull(coords, "The parameter 'coords' must not be null").x, coords.y, color);
    }

    public Gene(Gene source) {
        this(Preconditions.checkNotNull(source, "The parameter 'source' must not be null").x, source.y, source.color);
    }

    public void render(Graphics2D graphics) {
        graphics.setColor(new Color(color[1], color[2], color[3], color[0]));
        graphics.fillPolygon(x, y, x.length);
    }
    
    @Override
    public boolean equals(Object value) {
        if (value instanceof Gene) {
            final Gene v = (Gene) value;
            if (!Utils.equals(x, v.x))
                return false;
            if (!Utils.equals(y, v.y))
                return false;
            if (!Utils.equals(color, v.color))
                return false;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append('{');
        for (int i = 0; i < x.length; i++) {
            b.append(x[i] + ":" + y[i]).append(", ");
        }
        b.append(color[0] + ":" + color[1] + ":" + color[2] + ":" + color[3]);
        b.append('}');
        return b.toString();
    }
    
    public static Gene deserialize(DataInputStream in) throws IOException {
        Preconditions.checkNotNull(in, "The parameter 'in' must not be null");
        final byte version = in.readByte();
        Preconditions.checkArgument(version == 0, "Unable to deserialize gene, version not supported");
        final int[] color = new int[4];
        color[0] = in.readByte() & 0xFF;
        color[1] = in.readByte() & 0xFF;
        color[2] = in.readByte() & 0xFF;
        color[3] = in.readByte() & 0xFF;
        final int length = in.readByte() & 0xFF;
        Preconditions.checkArgument(length >= 3, "Unable to deserialize gene, too few coordinates");
        final int[] x = new int[length], y = new int[length];
        for (int i = 0; i < length; i++) {
            x[i] = in.readShort();
            y[i] = in.readShort();
        }
        return new Gene(x, y, color, false);
    }
    
    public static void serialize(Gene gene, DataOutputStream out) throws IOException {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        Preconditions.checkNotNull(out, "The parameter 'out' must not be null");
        out.writeByte(0); // version of serialization format
        out.writeByte(gene.color[0]);
        out.writeByte(gene.color[1]);
        out.writeByte(gene.color[2]);
        out.writeByte(gene.color[3]);
        final int length = gene.x.length;
        Preconditions.checkState(length <= 255, "Unable to serialize gene, too many coordinates");
        out.writeByte(length);
        for (int i = 0; i < length; i++) {
            Preconditions.checkState(gene.x[i] == (short) gene.x[i] && gene.y[i] == (short) gene.y[i], "Unable to serialize gene, coordinate out of bounds");
            out.writeShort(gene.x[i]);
            out.writeShort(gene.y[i]);
        }
    }
}
