package main.compiler.codegeneration;

import java.io.File;

public class JackCompiler {
    public static void main(String[] args) {
        if(args.length != 1){
            System.out.println("Wrong argument quantity");
            System.exit(0);
        }

        File input = new File(args[0]);

        if(input.isFile()){
            File outputVMFile = new File(input.getAbsolutePath().replace(".jack",".vm"));
            CompilationEngine compiler = new CompilationEngine(input,outputVMFile);
            compiler.compileClass();
        }
        else if(input.isDirectory()){
            File[] allFiles = input.listFiles();
            if(allFiles != null){
                for(File file: allFiles){
                    if(file.getName().endsWith(".jack")){
                        File outputVMFile = new File(file.getAbsolutePath().replace(".jack",".vm"));
                        CompilationEngine compiler = new CompilationEngine(file,outputVMFile);
                        compiler.compileClass();
                    }
                }
            }
        }

    }


}