package main.compiler.syntaxanalysis;

import java.io.File;
import java.io.IOException;
public class JackAnalyzer {
    public static void main(String[] args) {

        if(args.length != 1){
            System.out.println("Wrong argument quantity");
            System.exit(0);
        }

        File input = new File(args[0]);

        if(input.isFile()){
            writeXMLFile(input);
            System.out.println("Written.");
            System.exit(0);
        }else if(input.isDirectory()){
            System.out.println("Given directory.");
            File[] allFiles = input.listFiles();
            if(allFiles != null) {
                File[] allJackFiles = new File[allFiles.length];
                int j = 0;
                for (File jackFile: allFiles) {
                    if(jackFile.getName().endsWith(".jack")){
                        allJackFiles[j] = jackFile;
                        j++;
                    }
                }

                int i = 0;
                while (allJackFiles[i] != null){
                    writeXMLFile(allJackFiles[i]);
                    i++;
                }
            }
            System.out.println("All .jack files are parsed.");
        }

    }

    public static String getFileNameWithOutExt(String filename){

        int dotIndex = filename.indexOf(".");
        if(dotIndex != -1){
            return filename.substring(0,dotIndex);
        }
        return filename;
    }

    private static void writeXMLFile(File input){
        String outputFileName = input.getAbsolutePath().replace(".jack",".xml");
        CompilationEngine compilationEngine = new CompilationEngine(input, new File(outputFileName));
        compilationEngine.tokenizer.advance();
        compilationEngine.compileClass();
        try {
            compilationEngine.writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}