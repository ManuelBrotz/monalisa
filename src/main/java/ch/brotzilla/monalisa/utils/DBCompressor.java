package ch.brotzilla.monalisa.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import ch.brotzilla.monalisa.db.Database;
import ch.brotzilla.monalisa.evolution.genes.RawGenome;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class DBCompressor {

    public static final String FileExtension = ".mlc";

    private final File dbFile, output;

    private String autoName(File dbFile) {
        String name = dbFile.getName();
        final int last = name.lastIndexOf('.');
        if (last > 0) {
            name = name.substring(0, last);
        }
        return name + FileExtension;
    }

    private List<byte[]> loadGenomes() throws IOException, SQLiteException {
        try (final Database db = Database.openDatabase(dbFile)) {
            List<byte[]> result = Lists.newArrayList();
            System.out.println("Loading genomes...");
            db.queryAllGenomesCompressed(result);
            System.out.println("Loaded " + result.size() + " genomes.");
            return result;
        }
    }
    
    private int processGenes(RawGenome genome, Multimap<Integer, Entry> genes) {
        int collisions = 0;
        genome.indexes = new int[genome.genes.length];
        outer: for (int i = 0; i < genome.genes.length; i++) {
            final int crc = genome.crcs[i];
            final byte[] gene = genome.genes[i];
            final Collection<Entry> knowns = genes.get(crc);
            for (final Entry known : knowns) {
                if (Utils.equals(gene, known.gene)) {
                    genome.indexes[i] = known.index;
                    continue outer;
                }
            }
            final Entry e = new Entry(crc, gene);
            genome.indexes[i] = e.index;
            knowns.add(e);
            if (knowns.size() > 1) {
                ++collisions;
            }
        }
        return collisions;
    }
    
    private void processGenomes(List<byte[]> input, List<RawGenome> genomes, Multimap<Integer, Entry> genes) throws IOException {
        System.out.println("Decoding genomes, merging genes...");
        final int steps = input.size() / 20;
        int collisions = 0, totalGenes = 0;
        for (int i = 0; i < input.size(); i++) {
            final byte[] rawGenome = input.get(i);
            final RawGenome genome = Compression.decodeRawGenome(rawGenome);
            totalGenes += genome.genes.length;
            genomes.add(genome);
            genome.computeCRCs();
            collisions += processGenes(genome, genes);
            genome.genes = null; // free some space 
            if (i > 0 && i % steps == 0) {
                System.out.print(Math.round((100.0 / input.size() * i) * 10) / 10 + "% ");
            }
        }
        if ((input.size() - 1) % steps != 0) {
            System.out.println("100%");
        } else {
            System.out.println();
        }
        System.out.println("Found " + genes.size() + " distinct genes out of " + totalGenes + ". (Got " + collisions + " crc collisions)");
        System.out.println("");
    }
    
    private static class Entry {
        
        private static int nextIndex = 0;
        
        public final int index, crc;
        public final byte[] gene;
        
        public Entry(int crc, byte[] gene) {
            this.index = nextIndex++;
            this.crc = crc;
            this.gene = gene;
        }
    }

    public DBCompressor(File dbFile, File output, boolean autoName) {
        Preconditions.checkNotNull(dbFile, "The parameter 'dbFile' must not be null");
        Preconditions.checkArgument(dbFile.isFile(), "The parameter 'dbFile' has to be a regular file (" + dbFile + ")");
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        output = new File(output, autoName(dbFile));
        Preconditions.checkArgument(!output.exists(), "The parameter 'output' already exists (" + output + ")");
        this.dbFile = dbFile;
        this.output = output;
    }

    public File getDBFile() {
        return dbFile;
    }

    public File getOutputFile() {
        return output;
    }

    public void compress() throws IOException, SQLiteException {
        try (final Database db = Database.openDatabase(dbFile)) {

            final List<byte[]> rawGenomes = loadGenomes();

            final List<RawGenome> genomes = Lists.newArrayListWithCapacity(rawGenomes.size());
            final Multimap<Integer, Entry> genes = ArrayListMultimap.create();

            processGenomes(rawGenomes, genomes, genes);
            
            rawGenomes.clear(); // free some space
        }
    }

    public static void main(String[] args) throws IOException, SQLiteException {
        final DBCompressor c = new DBCompressor(new File("./data/output/vaduz.mldb"), new File("./data/output/"), true);
        c.compress();
    }

}
