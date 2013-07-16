package ch.brotzilla.monalisa;

import java.awt.Color;
import java.awt.Graphics2D;

import com.google.common.base.Preconditions;

public class Gene {
	
	public final int[] x, y, color;
	
	public Gene(int[] x, int[] y, int[] color) {
		Preconditions.checkNotNull(x, "The parameter 'x' must not be null");
		Preconditions.checkArgument(x.length == 3, "The parameter 'x' must be of length 3");
		Preconditions.checkNotNull(y, "The parameter 'y' must not be null");
		Preconditions.checkArgument(y.length == 3, "The parameter 'y' must be of length 3");
		Preconditions.checkNotNull(color, "The parameter 'color' must not be null");
		Preconditions.checkArgument(color.length == 4, "The parameter 'color' must be of length 4");
		this.x = new int[] {x[0], x[1], x[2]};
		this.y = new int[] {y[0], y[1], y[2]};
		this.color = new int[] {color[0], color[1], color[2], color[3]};
	}

	public Gene(int[] x, int[] y, Color color) {
		Preconditions.checkNotNull(x, "The parameter 'x' must not be null");
		Preconditions.checkArgument(x.length == 3, "The parameter 'x' must be of length 3");
		Preconditions.checkNotNull(y, "The parameter 'y' must not be null");
		Preconditions.checkArgument(y.length == 3, "The parameter 'y' must be of length 3");
		Preconditions.checkNotNull(color, "The parameter 'color' must not be null");
		this.x = new int[] {x[0], x[1], x[2]};
		this.y = new int[] {y[0], y[1], y[2]};
		this.color = new int[] {color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()};
	}

	public Gene(int[] x, int[] y, int color) {
		Preconditions.checkNotNull(x, "The parameter 'x' must not be null");
		Preconditions.checkArgument(x.length == 3, "The parameter 'x' must be of length 3");
		Preconditions.checkNotNull(y, "The parameter 'y' must not be null");
		Preconditions.checkArgument(y.length == 3, "The parameter 'y' must be of length 3");
		this.x = new int[] {x[0], x[1], x[2]};
		this.y = new int[] {y[0], y[1], y[2]};
		final Color tmp = new Color(color, true);
		this.color = new int[] {tmp.getAlpha(), tmp.getRed(), tmp.getGreen(), tmp.getBlue()};
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
		graphics.fillPolygon(x, y, 3);
	}
	
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append('{');
		for (int i = 0; i < 3; i++) {
			b.append(x[i]+":"+y[i]).append(", ");
		}
		b.append(color[0]+":"+color[1]+":"+color[2]+":"+color[3]);
		b.append('}');
		return b.toString();
	}
}
