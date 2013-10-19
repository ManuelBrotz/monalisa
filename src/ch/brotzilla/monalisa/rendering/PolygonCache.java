package ch.brotzilla.monalisa.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.images.ImageType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

public class PolygonCache {

    private final int width, height;
    
    private final BlockingQueue<Genome> queue = Queues.newLinkedBlockingQueue();
    private final ConcurrentMap<Gene, CacheEntry> cache = Maps.newConcurrentMap();
    private final ExecutorService workerThread = Executors.newFixedThreadPool(1);
    
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
        return cache.size();
    }

    public CacheEntry get(Gene gene) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        final CacheEntry entry = cache.get(gene);
        if (entry != null) {
            entry.touch();
        }
        return entry;
    }
    
    public void submit(Genome genome) {
        Preconditions.checkNotNull(genome, "The parameter 'genome' must not be null");
        queue.offer(genome);
    }
    
    public void shutdown() {
        workerThread.shutdownNow();
        try {
            workerThread.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }
    
    private class WorkerThread implements Runnable {

        private final Color Transparent = new Color(0, 0, 0, 0);
        
        private void cleanupPolygonCache() {
            final Iterator<CacheEntry> it = cache.values().iterator();
            while (it.hasNext()) {
                final CacheEntry e = it.next();
                if (e.getTouchedSince() > 2000) {
                    it.remove();
                }
            }
        }
        
        private boolean sleep(long ms) {
            final long start = System.currentTimeMillis();
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                if (workerThread.isShutdown()) {
                    return false;
                }
                final long now = System.currentTimeMillis();
                if (now - start < ms * 0.9) {
                    return sleep(now - start);
                }
            }
            return !workerThread.isShutdown();
        }
        
        private CacheEntry renderPolygon(Gene gene, Image image) {
            
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
        
        private void processQueue(Image image) {
            final List<Genome> genomes = Lists.newLinkedList();
            final Set<Gene> genes = Sets.newHashSet();
            queue.drainTo(genomes);
            for (final Genome genome : genomes) {
                for (final Gene gene : genome.genes) {
                    genes.add(gene);
                }
            }
            for (final Gene gene : genes) {
                if (!cache.containsKey(gene)) {
                    final CacheEntry entry = renderPolygon(gene, image);
                    cache.put(gene, entry);
                }
            }
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            final Image image = new Image(ImageType.ARGB, getWidth(), getHeight());
            while (sleep(2000)) {
                cleanupPolygonCache();
                processQueue(image);
            }
        }
    }
}
