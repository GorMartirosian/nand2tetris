package main.compiler.syntaxanalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class CodeBufferedReader extends BufferedReader {

    public CodeBufferedReader(FileReader fileReader){
        super(fileReader);
    }

    @Override
    public String readLine(){
        try {
            return super.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void mark(int readAheadLimit){
        try {
            super.mark(readAheadLimit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset(){
        try {
            super.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public char readChar(){
        try {
            return (char) super.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }







}
