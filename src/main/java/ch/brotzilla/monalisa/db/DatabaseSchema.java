package ch.brotzilla.monalisa.db;

import ch.brotzilla.monalisa.db.schema.DataType;
import ch.brotzilla.monalisa.db.schema.Field;
import ch.brotzilla.monalisa.db.schema.Index;
import ch.brotzilla.monalisa.db.schema.Indexes;
import ch.brotzilla.monalisa.db.schema.Schema;
import ch.brotzilla.monalisa.db.schema.Table;
import ch.brotzilla.monalisa.db.schema.Tables;

public class DatabaseSchema extends Schema {

    public static class TblGenomes extends Table {

        public static final Field fFitness = new Field("fitness", DataType.Real, false, false);
        public static final Field fSelected = new Field("selected", DataType.Integer, false, true);
        public static final Field fPolygons = new Field("polygons", DataType.Integer, false, false);
        public static final Field fData = new Field("data", DataType.Blob, false, false);

        public TblGenomes() {
            super("genomes", fFitness, fSelected, fPolygons, fData);
        }
    }
    
    public static class TblFiles extends Table {
        
        public static final Field fId = new Field("id", DataType.Text, false, true);
        public static final Field fOriginalName = new Field("originalName", DataType.Text, true, false);
        public static final Field fCompressed = new Field("compressed", DataType.Integer, false, false);
        public static final Field fData = new Field("data", DataType.Blob, true, false);
        
        public TblFiles() {
            super("files", fId, fOriginalName, fCompressed, fData);
        }
    }
    
    public static class TblSettings extends Table {
        
        public static final Field fId = new Field("id", DataType.Text, false, true);
        public static final Field fValue = new Field("value", DataType.Blob, true, false);
        
        public TblSettings() {
            super("settings", fId, fValue);
        }
    }
    
    public static final TblGenomes tblGenomes = new TblGenomes();
    public static final TblFiles tblFiles = new TblFiles();
    public static final TblSettings tblSettings = new TblSettings();
    
    public static final Index idxGenomesFitness = new Index("index_genomes_fitness", tblGenomes, TblGenomes.fFitness);
    public static final Index idxFilesName = new Index("index_files_name", tblFiles, TblFiles.fId);
    public static final Index idxSettingsId = new Index("index_settings_id", tblSettings, TblSettings.fId);
    
    public DatabaseSchema() {
        super(new Tables(tblGenomes, tblFiles, tblSettings), 
                new Indexes(idxGenomesFitness, idxFilesName, idxSettingsId)
        );
    }
}