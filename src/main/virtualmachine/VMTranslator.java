package main.virtualmachine;

import java.io.File;

public class VMTranslator {
    public static void main(String[] args) {

        if(args.length == 1){
            if (args[0].equals("--help")) {
                programHelper();
            }
            if(vmFileExistsInCurrentWorkingDirectory(args[0])){
                File vmCodeInputFile = new File(System.getProperty("user.dir") + "\\" + args[0]);
                String outAsmFileName = CodeWriter.getFileNameWithOutExt(args[0]);
                File vmCodeOutputAsmFile = new File(System.getProperty("user.dir") + "\\" + outAsmFileName + ".asm");
                Parser parser = new Parser(vmCodeInputFile);
                CodeWriter writerAsm = new CodeWriter(vmCodeOutputAsmFile);
                translateVMFile(parser,writerAsm);
                writerAsm.close();
            }else if (vmFileExistsByAbsolutePath(args[0])) {
                File vmCodeInputFile = new File(args[0]);
                String outAsmFileName = CodeWriter.getFileNameWithOutExt(args[0]);
                File vmCodeOutputAsmFile = new File(outAsmFileName + ".asm");
                Parser parser = new Parser(vmCodeInputFile);
                CodeWriter writerAsm = new CodeWriter(vmCodeOutputAsmFile);
                translateVMFile(parser,writerAsm);
                writerAsm.close();
            }else if (isDirectory(args[0])){
                File vmDirectory = new File(args[0]);
                File[] vmCodeInputFiles = getDirectoryVMFiles(vmDirectory);
                if(vmCodeInputFiles == null){
                    System.out.println("Supplied directory does not contain .vm files.");
                    System.exit(0);
                }


                Parser parser = new Parser(vmCodeInputFiles[0]);

                String outputFileNameString = vmCodeInputFiles[0].getName();
                outputFileNameString = outputFileNameString.substring(0,outputFileNameString.indexOf(".vm"));
                File vmCodeOutputFile = new File(vmDirectory.getAbsolutePath() + "\\" + outputFileNameString + ".asm");

                CodeWriter writerAsm = new CodeWriter(vmCodeOutputFile);
                translateVMFile(parser,writerAsm);
                for(int i = 1; i < vmCodeInputFiles.length; i++){
                    if(vmCodeInputFiles[i] == null){
                        break;
                    }
                    writerAsm.setFileName(vmCodeInputFiles[i].getName());
                    parser = new Parser(vmCodeInputFiles[i]);
                    translateVMFile(parser,writerAsm);
                }
                writerAsm.close();
            } else {
                System.out.println("Sorry could not find the supplied file.");
                System.exit(0);
            }

        } else {
            System.out.println("type \"java VMTranslator --help\" for more information.");
            System.exit(0);
        }

    }


    // must check if file exists in current working directory
    private static boolean vmFileExistsInCurrentWorkingDirectory(String inputFileName){
        File vmCodeInputFile = new File(System.getProperty("user.dir") + "\\" + inputFileName);
        return vmCodeInputFile.isFile() && vmCodeInputFile.getName().endsWith(".vm");
    }
    private static boolean vmFileExistsByAbsolutePath(String inputFileName){
        File vmCodeInputFile = new File(inputFileName);
        return vmCodeInputFile.isFile() && vmCodeInputFile.getName().endsWith(".vm");
    }
    private static boolean isDirectory(String path){
        File directory = new File(path);
        return directory.isDirectory();
    }

    private static void programHelper(){
        System.out.println("Program: VM Translator");
        System.out.println("Input: A file containing VM code (must have .vm extension).");
        System.out.println("Output: Creates a Hack assembly language file in the directory of the input file.");
        System.out.println("Usage: java VMTranslator <here goes your file>.");
        System.exit(0);
    }
    private static File[] getDirectoryVMFiles(File directory){
        File[] vmFiles = null;
        if(directory.isDirectory()){
            File[] files = directory.listFiles();
            if(files != null){
                vmFiles = new File[files.length];
                int j = 0;
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".vm")) {
                        vmFiles[j++] = file;
                    }
                }
            }
        }
        return vmFiles;
    }
    private static void translateVMFile(Parser parser, CodeWriter writerAsm){
        while (parser.currentLine != null){
            if(parser.currentCommandType == parser.C_ARITHMETIC){
                writerAsm.writeArithmetic(parser.currentCommand);
            }else if(parser.currentCommandType == Parser.C_POP){
                int index = Integer.parseInt(parser.splitCommand[2]);
                String segment = parser.splitCommand[1];
                writerAsm.writePushPop(Parser.C_POP,segment,index);
            }else if(parser.currentCommandType == Parser.C_PUSH){
                int index = Integer.parseInt(parser.splitCommand[2]);
                String segment = parser.splitCommand[1];
                writerAsm.writePushPop(Parser.C_PUSH,segment,index);
            }else if(parser.currentCommandType == Parser.C_FUNCTION){
                int localVarCount = Integer.parseInt(parser.splitCommand[2]);
                String funcName = parser.splitCommand[1];
                writerAsm.currentFunctionName = funcName;
                writerAsm.writeFunction(funcName,localVarCount);
            }else if(parser.currentCommandType == Parser.C_CALL){
                int argCount = Integer.parseInt(parser.splitCommand[2]);
                String funcName = parser.splitCommand[1];
                writerAsm.writeCall(funcName,argCount);
            }else if(parser.currentCommandType == Parser.C_RETURN){
                writerAsm.writeReturn();
            } else if (parser.currentCommandType == Parser.C_GOTO) {
                writerAsm.writeGoto(parser.splitCommand[1]);
            }else if(parser.currentCommandType == Parser.C_IF){
                writerAsm.writeIf(parser.splitCommand[1]);
            }else if(parser.currentCommandType == Parser.C_LABEL){
                writerAsm.writeLabel(parser.splitCommand[1]);
            }
            parser.advance();
        }
    }
}