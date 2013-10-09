package ch.brotzilla.monalisa.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.brotzilla.monalisa.images.ImageARGB;
import ch.brotzilla.monalisa.utils.MersenneTwister;

import com.google.common.base.Stopwatch;

public class RenderTest {

    private RenderTest() {}
    
    public static final Color HalfRed = new Color(255, 0, 0, 127);
    public static final Color Transparent = new Color(0, 0, 0, 0);

    public static final int Width = 400, Height = 400;
    public static final int Type = BufferedImage.TYPE_INT_ARGB;
    
    public static final int[][] randomPolygon = randomPolygon(Width, Height, "IchLiebeMeinenSchatziSchnügel".hashCode());
    
    public static final ImageARGB image = new ImageARGB(Width, Height, false), cachedPolygon = new ImageARGB(Width, Height, true);
    
    public static final int Loops = 20000;
    
    public static class Trimmed {
        
        public final int x, y;
        public final BufferedImage image;
        
        public Trimmed(int x, int y, BufferedImage image) {
            this.x = x;
            this.y = y;
            this.image = image;
        }
        
        public void draw(Graphics2D g) {
            g.drawImage(image, x, y, null);
        }
        
        public void draw(ImageARGB target) {
            draw(target.graphics);
        }
        
        public BufferedImage untrimmed() {
            final BufferedImage result = new BufferedImage(Width, Height, Type);
            final Graphics2D g = result.createGraphics();
            g.setBackground(Transparent);
            g.clearRect(0, 0, Width, Height);
            draw(g);
            return result;
        }
    }
    
    public static void main(String[] args) throws IOException {
        
        clear(cachedPolygon);
        renderPolygon(cachedPolygon, randomPolygon, HalfRed);
        ImageIO.write(cachedPolygon.image, "PNG", new File("./output/test_cached.png"));
        
        final Trimmed trimmed = trim(cachedPolygon);
        ImageIO.write(trimmed.image, "PNG", new File("./output/test_trimmed.png"));
        ImageIO.write(trimmed.untrimmed(), "PNG", new File("./output/test_untrimmed.png"));

        final Stopwatch w = new Stopwatch();
        
        clear(image);
        w.start();
        for (int i = 0; i < Loops; i++) {
            renderPolygon(image, randomPolygon, HalfRed);
        }
        w.stop();
        System.out.println("Time for rendering: " + w.elapsedMillis() + " ms");
        
        clear(image);
        w.reset().start();
        for (int i = 0; i < Loops; i++) {
            draw(image, cachedPolygon.image);
        }
        w.stop();
        System.out.println("Time for blitting: " + w.elapsedMillis() + " ms");

        clear(image);
        w.reset().start();
        for (int i = 0; i < Loops; i++) {
            trimmed.draw(image);
        }
        w.stop();
        System.out.println("Time for blitting trimmed: " + w.elapsedMillis() + " ms");
    }
    
    public static int[][] randomPolygon(int width, int height, int seed) {
        final MersenneTwister rng = new MersenneTwister(seed);
        final int length = 3 + rng.nextInt(8);
        final int[] x = new int[length], y = new int[length];
        for (int i = 0; i < length; i++) {
            x[i] = rng.nextInt(width - 100) + 50;
            y[i] = rng.nextInt(height - 100) + 50;
        }
        return new int[][] {x, y};
    }
    
    public static void clear(ImageARGB target) {
        target.graphics.setBackground(Transparent);
        target.graphics.clearRect(0, 0, target.width, target.height);
    }
    
    public static void renderPolygon(ImageARGB target, int[][] polygon, Color color) {
        target.graphics.setColor(color);
        target.graphics.fillPolygon(polygon[0], polygon[1], polygon[0].length);
    }
    
    public static void draw(ImageARGB target, BufferedImage source) {
        target.graphics.drawImage(source, 0, 0, null);
    }
    
    public static Trimmed trim(ImageARGB input) {
        final int w = input.width, h = input.height;
        final int[] data = input.readData();
        
        int lx = w, rx = 0, ty = h, by = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0, i = y * w; i < y * w + w; x++, i++) {
                final int argb = data[i];
                final int a = (argb >> 24) & 0x000000FF;
                if (a > 0) {
                    if (x < lx) lx = x;
                    if (x > rx) rx = x;
                    if (y < ty) ty = y;
                    if (y > by) by = y;
                }
            }
        }
        ++rx; ++by;
        
        final int width = rx - lx, height = by - ty;
        final BufferedImage result = new BufferedImage(width, height, Type);
        final Graphics2D g = result.createGraphics();
        g.setBackground(Transparent);
        g.clearRect(0, 0, width, height);
        g.drawImage(input.image, 0, 0, width, height, lx, ty, rx, by, null);
        
        return new Trimmed(lx, ty, result);
    }
}
