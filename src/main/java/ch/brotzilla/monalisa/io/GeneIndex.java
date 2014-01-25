package ch.brotzilla.monalisa.io;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ch.brotzilla.monalisa.db.GeneEntry;
import ch.brotzilla.monalisa.evolution.genes.Gene;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class GeneIndex {

    private final Multimap<Integer, GeneEntry> map;
    private final Map<Integer, GeneEntry> entryIndex;
    private final Map<Integer, Gene> geneIndex;
    
    private int size = 0, maxIndex = 0;
    
    public GeneIndex() {
        this.map = ArrayListMultimap.create();
        this.entryIndex = Maps.newHashMap();
        this.geneIndex = Maps.newHashMap();
    }

    public synchronized void populate(List<GeneEntry> genes) {
        Preconditions.checkNotNull(genes, "The parameter 'genes' must not be null");
        clear();
        try {
            for (final GeneEntry e : genes) {
                int id = e.getId();
                int crc = e.getCrc();
                map.put(crc, e);
                if (entryIndex.get(id) != null || geneIndex.get(id) != null) {
                    throw new IllegalArgumentException("Index " + id + " is already taken");
                }
                entryIndex.put(id, e);
                if (id > maxIndex) {
                    maxIndex = id;
                }
                ++size;
            }
        } catch (Exception e) {
            clear();
            throw e;
        }
    }
    
    public synchronized Gene get(int index) throws IOException {
        Preconditions.checkArgument(index > 0, "Parameter 'index' out of bounds: " + index);
        Preconditions.checkArgument(index <= size, "Parameter 'index' out of bounds: " + index);
        Gene result = geneIndex.get(index);
        if (result == null) {
            final GeneEntry e = entryIndex.get(index);
            if (e == null) {
                throw new IllegalStateException("Unable to retrieve entry at index " + index);
            }
            result = Gene.deserialize(e);
            geneIndex.put(index, result);
        }
        return result;
    }
    
    public synchronized int find(Gene gene) {
        Preconditions.checkNotNull(gene, "The parameter 'gene' must not be null");
        
    }
    
    public int size() {
        return size;
    }

    public synchronized void clear() {
        map.clear();
        entryIndex.clear();
        geneIndex.clear();
        maxIndex = 0;
        size = 0;
    }
}
