// Created by Manuel Brotz, 2014.  Released into the public domain.
//
// Source is licensed for any use, provided this copyright notice is retained.
// No warranty for any purpose whatsoever is implied or expressed.  The author
// is not liable for any losses of any kind, direct or indirect, which result
// from the use of this software.

package ch.brotzilla.util;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * <p>Very simple command line parser.</p>
 * <p>
 * - Parses a single line into an array of strings.<br> 
 * - Supports escaped characters and double quotes.<br>
 * </p>
 * 
 * <p>
 * <b>Example input:</b><br>
 * <code>this is\ a test --command "" with "empty quotes" "\"\""</code><br><br>
 * <b>Example output:</b><br>
 * <code>
 * this<br>
 * is a<br>
 * test<br>
 * --command<br>
 * <br>
 * with<br>
 * empty quotes<br>
 * ""<br>
 * </code>
 * </p>
 * 
 * @author Manuel Brotz
 */

public class CmdLine {

    private CmdLine() {}

    public static class CmdScanner {

        private final String input;
        private final int length;
        
        private int position;
        
        public CmdScanner(String input) {
            Preconditions.checkNotNull(input, "The parameter 'input' must not be null");
            this.input = input;
            this.length = input.length();
            this.position = 0;
        }
        
        public String getInput() {
            return input;
        }
        
        public boolean isBeginOfLine() {
            return position == 0;
        }
        
        public boolean isEndOfLine() {
            return position >= length;
        }
        
        public boolean hasNext() {
            return position < length && length > 0;
        }
        
        public int getLength() {
            return length;
        }
        
        public int getPosition() {
            return position;
        }
        
        public char next() {
            if (position < length && length > 0) {
                return input.charAt(position++);
            }
            return 0;
        }
        
        public char peek() {
            if (position < length && length > 0) {
                return input.charAt(position + 1);
            }
            return 0;
        }
    }

    public static class CmdTokenizer {

        private final CmdScanner scanner;
        private final StringBuilder token;
        
        private char c = 0;
        private boolean escaped = false;
        private boolean end = false;
        
        private void nextChar() {
            if (scanner.isEndOfLine()) {
                end = true;
                escaped = false;
                this.c = 0;
            }
            final char c = scanner.next();
            if (c == '\\') {
                if (scanner.isEndOfLine()) {
                    end = true;
                    escaped = false;
                    this.c = 0;
                    return;
                }
                this.escaped = true;
                this.c = scanner.next();
            } else {
                this.escaped = false;
                this.c = c;
            }
        }
        
        private void skip() {
            while (Character.isWhitespace(c)) {
                if (escaped) {
                    return;
                }
                nextChar();
            }
        }
        
        private String finish(boolean quoted) {
            final String tok = token.toString();
            if (!quoted && tok.isEmpty()) {
                return next();
            }
            return tok;
        }
        
        public CmdTokenizer(String input) {
            this.scanner = new CmdScanner(input);
            this.token = new StringBuilder();
            nextChar();
        }
        
        public String next() {
            if (end) {
                return null;
            }
            
            token.setLength(0);
            skip();
            
            boolean quoted = false;
            while (!end) {
                if (escaped) {
                    token.append(c);
                    nextChar();
                    continue;
                }
                if (!quoted) {
                    if (c == '"') {
                       if (token.length() == 0) {
                           quoted = true;
                           nextChar();
                           continue;
                       } else {
                           return finish(false);
                       }
                    } else if (Character.isWhitespace(c)) {
                        return finish(false);
                    } else {
                        token.append(c);
                        nextChar();
                        continue;
                    }
                } else {
                    if (c == '"') {
                        nextChar();
                        return finish(true);
                    } else {
                        token.append(c);
                        nextChar();
                        continue;
                    }
                }
            }
           
            return finish(quoted);
        }
    }

    public static int parse(String cmdline, List<String> output) {
        Preconditions.checkNotNull(output, "The parameter 'output' must not be null");
        
        if (cmdline == null || cmdline.trim().isEmpty()) {
            return 0;
        }

        final CmdTokenizer tokenizer = new CmdTokenizer(cmdline);

        int count = 0;
        String token = tokenizer.next();
        while (token != null) {
            output.add(token);
            ++count;
            token = tokenizer.next();
        }

        return count;
    }
    
    public static String[] parse(String cmdline) {
        final List<String> list = Lists.newArrayList();
        final int count = parse(cmdline, list);
        return list.toArray(new String[count]);
    }
    
}
