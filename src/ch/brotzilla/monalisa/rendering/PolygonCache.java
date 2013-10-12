package ch.brotzilla.monalisa.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.images.ImageType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class PolygonCache {

    private final int width, height;
    
    private final ConcurrentMap<Gene, TempEntry> tempCache = Maps.newConcurrentMap();
    private final ConcurrentMap<Gene, CacheEntry> polygonCache = Maps.newConcurrentMap();
    private final ExecutorService workerThread = Executors.newFixedThreadPool(1);
    
    private void touch(Gene gene) {
        final TempEntry temp = tempCache.get(gene);
        if (temp == null) {
            tempCache.putIfAbsent(gene, new TempEntry());
        } else {
            temp.touch();
        }
    }
    
    public PolygonCache(int width, int height) {
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zero");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        this.width = width;
        this.height = height;
        workerThread.submit(new WorkerThread());
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getSize() {
        return polygonCache.size();
    }

    public CacheEntry get(Gene gene) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        final CacheEntry entry = polygonCache.get(gene);
        if (entry == null) {
            touch(gene);
            return null;
        }
        if (!gene.equals(entry.getGene())) {
            throw new IllegalStateException("Genes are not equal: " + gene.toString() + " != " + entry.getGene().toString());
        }
        entry.touch();
        return entry;
    }
    
    private class WorkerThread implements Runnable {

        private final Color Transparent = new Color(0, 0, 0, 0);
        
        private void processTempCache(LinkedList<Gene> output) {
            final Iterator<Entry<Gene, TempEntry>> it = tempCache.entrySet().iterator();
            while (it.hasNext()) {
                final Entry<Gene, TempEntry> e = it.next();
                final TempEntry t = e.getValue();
                if (t.getTouchedSince() > 2000) {
                    it.remove();
                    continue;
                } else if (t.getCreatedSince() > 10000) {
                    output.add(e.getKey());
                }
            }
        }
        
        private void cleanupPolygonCache() {
            final Iterator<CacheEntry> it = polygonCache.values().iterator();
            while (it.hasNext()) {
                final CacheEntry e = it.next();
                if (e.getTouchedSince() > 60000) {
                    it.remove();
                }
            }
        }
        
        private void sleep(long ms) {
            final long start = System.currentTimeMillis();
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                System.out.println("Sleep interrupted...");
                final long now = System.currentTimeMillis();
                if (now - start < ms * 0.9) {
                    sleep(now - start);
                }
            }
        }
        
        public CacheEntry renderPolygon(Gene gene, Image image) {
            
            image.getGraphics().setBackground(Transparent);
            image.getGraphics().clearRect(0, 0, image.getWidth(), image.getHeight());
            gene.render(image.getGraphics());
            
            final int w = image.getWidth(), h = image.getHeight();
            final int[] data = image.readData();
            
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
            final BufferedImage result = ImageType.ARGB.createBufferedImage(width, height);
            final Graphics2D g = result.createGraphics();
            g.setBackground(Transparent);
            g.clearRect(0, 0, width, height);
            g.drawImage(image.getImage(), 0, 0, width, height, lx, ty, rx, by, null);
            
            return new CacheEntry(gene, result, lx, ty);
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            final LinkedList<Gene> worklist = new LinkedList<Gene>();
            final Image image = new Image(ImageType.ARGB, getWidth(), getHeight());
            while (true) {
                sleep(1000);
                cleanupPolygonCache();
                processTempCache(worklist);
                if (worklist.size() > 0) {
                    for (final Gene gene : worklist) {
                        polygonCache.put(gene, renderPolygon(gene, image));
                    }
                    worklist.clear();
                }
            }
        }
    }
}
