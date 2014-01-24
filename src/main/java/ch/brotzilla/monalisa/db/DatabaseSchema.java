package ch.brotzilla.monalisa.db;

import ch.brotzilla.monalisa.db.schema.DataType;
import ch.brotzilla.monalisa.db.schema.Field;
import ch.brotzilla.monalisa.db.schema.Index;
import ch.brotzilla.monalisa.db.schema.Indexes;
import ch.brotzilla.monalisa.db.schema.Schema;
import ch.brotzilla.monalisa.db.schema.Table;
import ch.brotzilla.monalisa.db.schema.Tables;

public class DatabaseSchema extends Schema {

    public static class TblGenes extends Table {
        
        public static final Field fId = new Field("id", DataType.Integer, false, true);
        public static final Field fCrc = new Field("crc", DataType.Integer, false, false);
        public static final Field fData = new Field("data", DataType.Blob, true, false);
        
        public TblGenes() {
            super("genes", fId, fCrc, fData);
        }
    }
    
    public static class TblGenomes extends Table {

        public static final Field fImprovements = new Field("improvements", DataType.Integer, false, true);
        public static final Field fFitness = new Field("fitness", DataType.Real, false, false);
        public static final Field fData = new Field("data", DataType.Blob, true, false);

        public TblGenomes() {
            super("genomes", fImprovements, fFitness, fData);
        }
    }
    
    public static class TblFiles extends Table {
        
        public static final Field fId = new Field("id", DataType.Text, false, true);
        public static final Field fOriginalName = new Field("originalName", DataType.Text, true, false);
        public static final Field fData = new Field("data", DataType.Blob, true, false);
        
        public TblFiles() {
            super("files", fId, fOriginalName, fData);
        }
    }
    
    public static final TblGenes tblGenes = new TblGenes();
    public static final TblGenomes tblGenomes = new TblGenomes();
    public static final TblFiles tblFiles = new TblFiles();
    
    public static final Index idxGenesId = new Index("index_genes_id", tblGenes, TblGenes.fId);
    public static final Index idxGenesCrc = new Index("index_genes_crc", tblGenes, TblGenes.fCrc);
    public static final Index idxGenomesImprovements = new Index("index_genomes_improvements", tblGenomes, TblGenomes.fImprovements);
    public static final Index idxGenomesFitness = new Index("index_genomes_fitness", tblGenomes, TblGenomes.fFitness);
    public static final Index idxFilesId = new Index("index_files_id", tblFiles, TblFiles.fId);
    
    public DatabaseSchema() {
        super(
                new Tables(tblGenes, tblGenomes, tblFiles), 
                new Indexes(idxGenesId, idxGenesCrc, idxGenomesImprovements, idxGenomesFitness, idxFilesId)
                );
    }
}