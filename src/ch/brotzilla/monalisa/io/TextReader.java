package ch.brotzilla.monalisa.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import com.google.common.base.Preconditions;

public class TextReader {

    protected final char[] charBuffer;
    protected final StringBuffer stringBuffer;
    
    public TextReader(int bufferSize) {
        Preconditions.checkArgument(bufferSize >= 1024, "The parameter 'bufferSize' has to be greater than or equal to 1024");
        this.charBuffer = new char[bufferSize];
        this.stringBuffer = new StringBuffer(bufferSize);
    }
    
    public TextReader() {
        this(1024 * 100);
    }

    public synchronized int readTextFile(File file, StringBuffer output) throws IOException {
        Preconditions.checkNotNull(file, "The parameter 'file' must not be null");
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        final Reader br = new BufferedReader(new FileReader(file));
        try {
            int size = 0;
            while (true) {
                final int len = br.read(charBuffer);
                if (len <= 0) break;
                size += len;
                stringBuffer.append(charBuffer, 0, len);
            }
            return size;
        } finally {
            br.close();
        }
    }
    
    public synchronized String readTextFile(File file) throws IOException {
        stringBuffer.setLength(0);
        readTextFile(file, stringBuffer);
        return stringBuffer.toString();
    }
}