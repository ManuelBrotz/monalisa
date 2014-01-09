// Created by Manuel Brotz, 2014.  Released into the public domain.
//
// Source is licensed for any use, provided this copyright notice is retained.
// No warranty for any purpose whatsoever is implied or expressed.  The author
// is not liable for any losses of any kind, direct or indirect, which result
// from the use of this software.

package ch.brotzilla.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import com.google.common.base.Preconditions;

/**
 * Utility class for buffered reading of text files.<br>
 * Synchronized for thread safety.
 * 
 * @author Manuel Brotz
 * 
 */
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
            return readTextFile(br, output);
        } finally {
            br.close();
        }
    }

    public synchronized int readTextFile(Reader reader, StringBuffer output) throws IOException {
        Preconditions.checkNotNull(reader, "The parameter 'reader' must not be null");
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        int size = 0;
        while (true) {
            final int len = reader.read(charBuffer);
            if (len <= 0)
                break;
            size += len;
            stringBuffer.append(charBuffer, 0, len);
        }
        return size;
    }

    public synchronized String readTextFile(File file) throws IOException {
        stringBuffer.setLength(0);
        readTextFile(file, stringBuffer);
        return stringBuffer.toString();
    }
    
    public synchronized String readTextFile(Reader reader) throws IOException {
        stringBuffer.setLength(0);
        readTextFile(reader, stringBuffer);
        return stringBuffer.toString();
    }

}