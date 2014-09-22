package ch.brotzilla.monalisa.intf;

public interface Builder<T> {

    public boolean isReady();
    
    public Builder<T> checkReady();
    
    public T build();
    
}
