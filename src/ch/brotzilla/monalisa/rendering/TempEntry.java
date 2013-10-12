package ch.brotzilla.monalisa.rendering;

public class TempEntry {
    
    private final long created;
    private long touched;
    
    public TempEntry() {
        this.created = System.currentTimeMillis();
        this.touched = created;
    }
    
    public long getCreated() {
        return created;
    }
    
    public int getCreatedSince() {
        return (int) (System.currentTimeMillis() - created);
    }
    
    public long getTouched() {
        return touched;
    }
    
    public long getTouchedSince() {
        return (int) (System.currentTimeMillis() - touched);
    }
    
    public void touch() {
        touched = System.currentTimeMillis();
    }
}