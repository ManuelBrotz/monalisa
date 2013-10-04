package ch.brotzilla.monalisa.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import ch.brotzilla.monalisa.images.ImageData;

public class Compression {

    private Compression() {}
    
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static ImageData decodeImageData(byte[] input) throws IOException {
        if (input == null || input.length == 0)
            return null;
        
        final ByteArrayInputStream bin = new ByteArrayInputStream(input);
        final GZIPInputStream gzin = new GZIPInputStream(bin);
        final DataInputStream din = new DataInputStream(gzin);
        
        return ImageData.read(din);
    }
    
    public static String decodeString(byte[] input) throws IOException {
        if (input == null || input.length == 0)
            return null;
        
        final ByteArrayInputStream bin = new ByteArrayInputStream(input);
        final GZIPInputStream gzin = new GZIPInputStream(bin);
        final DataInputStream din = new DataInputStream(gzin);
        
        final int len = din.readInt();
        final byte[] utf8 = new byte[len];
        int read = din.read(utf8, 0, len);
        int total = read;
        while (total < len) {
            read = din.read(utf8, total, len - total);
            if (read == -1)
                throw new IOException("Unable to decode string");
            total += read;
        }
        
        return new String(utf8, UTF8);
    }

    public static byte[] encode(ImageData data) throws IOException {
        if (data == null)
            return null;
        
        final ByteArrayOutputStream bout = new ByteArrayOutputStream(100 * 1024);
        final GZIPOutputStream gzout = new GZIPOutputStream(bout);
        final DataOutputStream dout = new DataOutputStream(gzout);
        
        ImageData.write(data, dout);
        dout.close();
        
        return bout.toByteArray();
    }

    public static byte[] encode(String input) throws IOException {
        if (input == null || input.length() == 0)
            return null;
        
        final ByteArrayOutputStream bout = new ByteArrayOutputStream(10 * 1024);
        final GZIPOutputStream gzout = new GZIPOutputStream(bout);
        final DataOutputStream dout = new DataOutputStream(gzout);

        final byte[] utf8 = input.getBytes(UTF8);
        dout.writeInt(utf8.length);
        dout.write(utf8, 0, utf8.length);
        dout.close();
        
        return bout.toByteArray();
    }
}
