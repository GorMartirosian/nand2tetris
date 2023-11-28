package main.compiler.codegeneration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class VMWriter {

    private final CodeBufferedWriter writer;

    VMWriter(String outputFileName){
        try {
            writer = new CodeBufferedWriter(new FileWriter(outputFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    VMWriter(File outputFile){
        this(outputFile.getAbsolutePath());
    }

    void writePush(Segments segment, int index){
        switch (segment){
            case CONSTANT -> writer.write("push constant ");
            case ARGUMENT -> writer.write("push argument ");
            case LOCAL -> writer.write("push local ");
            case STATIC -> writer.write("push static ");
            case THAT -> writer.write("push that ");
            case THIS -> writer.write("push this ");
            case POINTER -> writer.write("push pointer ");
            case TEMP -> writer.write("push temp ");
        }
        writer.write(index + "\n");
    }

    void writePop(Segments segment, int index){
        switch (segment){
            case CONSTANT -> writer.write("pop constant ");
            case ARGUMENT -> writer.write("pop argument ");
            case LOCAL -> writer.write("pop local ");
            case STATIC -> writer.write("pop static ");
            case THAT -> writer.write("pop that ");
            case THIS -> writer.write("pop this ");
            case POINTER -> writer.write("pop pointer ");
            case TEMP -> writer.write("pop temp ");
        }
        writer.write(index + "\n");
    }

    void writeArithmetic(Commands command){
        switch (command){
            case ADD -> writer.write("add\n");
            case SUB -> writer.write("sub\n");
            case NEG -> writer.write("neg\n");
            case EQ -> writer.write("eq\n");
            case GT -> writer.write("gt\n");
            case LT -> writer.write("lt\n");
            case AND -> writer.write("and\n");
            case OR -> writer.write("or\n");
            case NOT -> writer.write("not\n");
        }
    }

    void writeLabel(String label){
        writer.write(label + "\n");
    }

    void writeGoto(String label){
        writer.write("goto " + label + "\n");
    }

    void writeIf(String label){
        writer.write("if-goto " + label + "\n");
    }

    void writeCall(String name, int nArgs){
        writer.write("call " + name + " " + nArgs + "\n");
    }

    void writeFunction(String name, int nVars){
        writer.write("function " + name + " " + nVars + "\n");
    }

    void writeReturn(){
        writer.write("return\n");
    }

    void close(){
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
