package ch.brotzilla.monalisa.rendering;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ch.brotzilla.monalisa.evolution.genes.Gene;
import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.images.Image;
import ch.brotzilla.monalisa.images.ImageType;
import ch.brotzilla.monalisa.vectorizer.RunState;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class PolygonCache {

    private final int width, height;

    private RunState state = RunState.Stopped;

    private final BlockingQueue<Genome> queue = Queues.newLinkedBlockingQueue();
    private final ConcurrentMap<Gene, CacheEntry> temp = Maps.newConcurrentMap();
    private final ConcurrentMap<Gene, CacheEntry> cache = Maps.newConcurrentMap();
    private ExecutorService workerThread;

    public PolygonCache(int width, int height) {
        Preconditions.checkArgument(width > 0, "The parameter 'width' has to be greater than zero");
        Preconditions.checkArgument(height > 0, "The parameter 'height' has to be greater than zero");
        this.width = width;
        this.height = height;
    }

    public RunState getState() {
        return state;
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
        CacheEntry entry = cache.get(gene);
        if (entry != null) {
            entry.touch();
            return entry;
        } else {
            entry = temp.get(gene);
            if (entry != null) {
                entry.touch();
            }
        }
        return null;
    }

    public synchronized void start() {
        if (state != RunState.Stopped) {
            throw new IllegalStateException("Unable to activate polygon cache");
        }
        workerThread = Executors.newFixedThreadPool(1);
        workerThread.submit(new WorkerThread(workerThread));
        state = RunState.Running;
    }

    public synchronized void stop() {
        if (state != RunState.Running) {
            return;
        }
        state = RunState.Stopping;
        try {
            workerThread.shutdown();
            try {
                workerThread.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queue.clear();
            temp.clear();
            cache.clear();
            workerThread = null;
        } finally {
            state = RunState.Stopped;
        }
    }
    
    public boolean offer(Genome genome) {
        if (genome == null || state != RunState.Running) {
            return false;
        }
        return queue.offer(genome);
    }

    private class WorkerThread implements Runnable {

        private final Color Transparent = new Color(0, 0, 0, 0);
        private final ExecutorService owner;

        private void renderPolygon(CacheEntry entry, Image buffer) {

            buffer.getGraphics().setBackground(Transparent);
            buffer.getGraphics().clearRect(0, 0, buffer.getWidth(), buffer.getHeight());
            entry.getGene().render(buffer.getGraphics());

            final int w = buffer.getWidth(), h = buffer.getHeight();
            final int[] data = buffer.readData();

            int lx = w, rx = 0, ty = h, by = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0, i = y * w; i < y * w + w; x++, i++) {
                    final int argb = data[i];
                    final int a = (argb >> 24) & 0x000000FF;
                    if (a > 0) {
                        if (x < lx)
                            lx = x;
                        if (x > rx)
                            rx = x;
                        if (y < ty)
                            ty = y;
                        if (y > by)
                            by = y;
                    }
                }
            }
            ++rx;
            ++by;

            final int width = rx - lx, height = by - ty;
            final BufferedImage result = ImageType.ARGB.createBufferedImage(width, height);
            final Graphics2D g = result.createGraphics();
            g.setBackground(Transparent);
            g.clearRect(0, 0, width, height);
            g.drawImage(buffer.getImage(), 0, 0, width, height, lx, ty, rx, by, null);

            entry.setImage(result, lx, ty);
        }

        private void drainQueue(final List<Genome> genomes) {
            try {
                final Genome head = queue.poll(500, TimeUnit.MILLISECONDS);
                if (head != null) {
                    genomes.add(head);
                    queue.drainTo(genomes);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void processQueue() {
            final List<Genome> genomes = Lists.newArrayList();
            drainQueue(genomes);
            for (final Genome genome : genomes) {
                for (final Gene gene : genome.genes) {
                    CacheEntry entry = temp.get(gene);
                    if (entry == null) {
                        entry = cache.get(gene);
                    }
                    if (entry != null) {
                        entry.touch();
                    } else {
                        temp.put(gene, new CacheEntry(gene));
                    }
                }
            }
        }

        private void processTemp(Image buffer) {
            final Collection<CacheEntry> entries = temp.values();
            final List<CacheEntry> good = Lists.newArrayListWithCapacity(entries.size());
            final List<CacheEntry> bad = Lists.newArrayListWithCapacity(entries.size());
            for (final CacheEntry entry : entries) {
                if (entry.getCreatedSince() >= 5000) {
                    if (entry.getTouchedSince() <= 100) {
                        good.add(entry);
                    } else {
                        bad.add(entry);
                    }
                }
            }
            for (final CacheEntry entry : bad) {
                temp.remove(entry.getGene());
            }
            for (final CacheEntry entry : good) {
                temp.remove(entry.getGene());
                renderPolygon(entry, buffer);
                cache.put(entry.getGene(), entry);
            }
        }

        private void processCache() {
            final List<CacheEntry> bad = Lists.newArrayList();
            for (final CacheEntry entry : cache.values()) {
                if (entry.getTouchedSince() >= 5000) {
                    bad.add(entry);
                }
            }
            for (final CacheEntry entry : bad) {
                cache.remove(entry.getGene());
            }
        }

        public WorkerThread(ExecutorService owner) {
            Preconditions.checkNotNull(owner, "The parameter 'owner' must not be null");
            this.owner = owner;
            owner.submit(this);
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            final Image buffer = new Image(ImageType.ARGB, getWidth(), getHeight());
            while (!owner.isShutdown()) {
                processQueue();
                processTemp(buffer);
                processCache();
            }
        }
    }
}
