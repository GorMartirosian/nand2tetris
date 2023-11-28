package main.compiler.syntaxanalysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CodeBufferedWriter extends BufferedWriter {

        public CodeBufferedWriter(FileWriter out) {
            super(out);
        }
        public void write(String str){
            try {
                super.write(str);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
}

