package ch.brotzilla.monalisa.utils;

import java.util.Arrays;

import com.google.common.base.Preconditions;

public class TickRate {
    
    private boolean started;
    private long start;
    private int count;

    private final double[] list;
    private final int size;
    private double sum;
    private int index;
    private int used;
    
    private synchronized void add(double rate) {
        sum -= list[index];
        sum += rate;
        list[index] = rate;
        if (++index >= size) {
            index = 0;
        }
        if (used < size) {
            ++used;
        }
    }
    
    public TickRate(int size) {
        Preconditions.checkArgument(size > 0, "The parameter 'size' has to be greater than zero");
        this.list = new double[size];
        this.size = size;
        reset();
    }
    
    public synchronized double getTickRate() {
        if (started && used > 0) {
            return sum / used;
        }
        return 0;
    }
    
    public synchronized void reset() {
        started = false;
        start = 0;
        count = 0;
        sum = 0;
        index = 0;
        used = 0;
        Arrays.fill(list, 0);
    }
    
    public synchronized void tick() {
        if (!started) {
            started = true;
            start = System.nanoTime();
        }
        ++count;
        long now = System.nanoTime();
        long elapsed = now - start;
        if (elapsed > 1000000000) {
            add(count * (1000000000d / elapsed));
            start = now;
            count = 0;
        }
    }
}