package main.virtualmachine;

import main.virtualmachine.writer.CodeBufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class CodeWriter {
    private final CodeBufferedWriter writer;
    int labelDeclarationSequenceNumber;
    public String currentFunctionName;
    int functionReturnSequenceNumber;
    static final HashMap<String ,String> vmSegmentNamesToAsmRegisters = new HashMap<>();
    String fileNameWithOutExt;
    static {
        vmSegmentNamesToAsmRegisters.put("local","LCL");
        vmSegmentNamesToAsmRegisters.put("argument", "ARG");
        vmSegmentNamesToAsmRegisters.put("this","THIS");
        vmSegmentNamesToAsmRegisters.put("that","THAT");
    }

    public CodeWriter(File outputAsmFile){
        String fileName = outputAsmFile.getName();
        this.fileNameWithOutExt = getFileNameWithOutExt(fileName);
        try {
            writer = new CodeBufferedWriter(new FileWriter(outputAsmFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeBootstrapCode();
    }

    private void writeBootstrapCode(){
        writeRawVMCommandAsAComment("SP = 256\n//call Sys.init 0");

        writeIntegerIntoDRegister(256);
        writer.write("@SP\nM=D\n");
        writeCall("Sys.init",0);
    }
    public void setFileName(String fileName){
        fileNameWithOutExt = getFileNameWithOutExt(fileName);
    }

    public void writeLabel(String label){
        writeRawVMCommandAsAComment("label " + label);
        writer.write("(" + fileNameWithOutExt + "." + currentFunctionName + "$" + label + ")\n");
    }

    public void writeGoto(String label){
        writeRawVMCommandAsAComment("goto " + label);
        writer.write("@" + fileNameWithOutExt + "." + currentFunctionName + "$" + label + "\n0;JMP\n");
    }
    public void writeIf(String label){
        writeRawVMCommandAsAComment("if-goto " + label);
        decrementSP();
        writer.write("@SP" + "\nA=M\nD=M\n@" + fileNameWithOutExt  + "." + currentFunctionName + "$" + label + "\nD;JNE\n");
    }
    public void writeFunction(String functionName, int nVars){
        writeRawVMCommandAsAComment("function " + functionName + " " + nVars);

        writer.write("(" + fileNameWithOutExt + "." + functionName + ")\n");
        for (int i = 0 ; i < nVars; i++){
            writePushPop(Parser.C_PUSH,"constant", 0);
        }
    }
    public void writeCall(String functionName, int nArgs){
        writeRawVMCommandAsAComment("call " + functionName + " " + nArgs);

        writePushCallerFrame(functionName);
        //ARG = SP - 5 - argCount
        int var = 5 + nArgs;
        writer.write("@" + var + "\nD=A\n@SP\nD=M-D\n@ARG\nM=D\n");
        //LCL = SP
        //goto functionName
        writer.write("@SP\nD=M\n@LCL\nM=D\n@" + fileNameWithOutExt + "." + functionName + "\n0;JMP\n");
        writer.write("(" + fileNameWithOutExt + "." + functionName + "$ret." + functionReturnSequenceNumber + "\n");
        functionReturnSequenceNumber++;
    }
    public void  writeReturn(){
        writeRawVMCommandAsAComment("return");

        //R13 = LCL
        writeVariableValueIntoDRegister("LCL");
        writer.write("@R13\nM=D\n");
        //R14 = *(R13 - 5) .... R14 has caller return address
        writeIntegerIntoDRegister(5);
        writer.write("R13\nA=M-D\nD=M\n@R14\nM=D\n");
        // *ARG = pop()
        decrementSP();
        writer.write("@SP\nA=M\nD=M\n@ARG\nA=M\nM=D\n");
        //SP = ARG + 1 .... makes SP point to the next memory cell after the callee returned value
        writer.write("@ARG\nD=M+1\n@SP\nM=D\n");
        //THAT = *(R13 - 1)
        writer.write("@R13\nA=M-1\nD=M\n@THAT\nM=D\n");

        //writing THIS = *(R13 - 2); ARG = *(R13 - 3); LCL = *(R13 - 4)
        String[] virtualRegs = {"THIS","ARG","LCL"};
        int i = 2;
        for(String virtualReg: virtualRegs){
            writer.write("@" + i + "\nD=A\n@R13\nA=M-D\nD=M\n@" + virtualReg + "\nM=D\n");
            i++;
        }

        //goto return address
        writer.write("@R14\nA=M\n0;JMP\n");
    }
    private void writePushCallerFrame(String functionName){

        //push return address
        writer.write("@" + fileNameWithOutExt + "." + functionName + "$ret." + functionReturnSequenceNumber +
                "\nD=A\n");
        writeDRegisterValueToStack();
        incrementSP();

        String[] virtualRegisters = {"LCL","ARG","THIS","THAT"};
        //push LCL,ARG,THIS,THAT
        for(String virtualRegister : virtualRegisters){
            writeVariableValueIntoDRegister(virtualRegister);
            writeDRegisterValueToStack();
            incrementSP();
        }
    }

    public static String getFileNameWithOutExt(String filename){

        int dotIndex = filename.indexOf(".");
        if(dotIndex != -1){
            return filename.substring(0,dotIndex);
        }
        return filename;
    }
    private void writeRawVMCommandAsAComment(String command){
        writer.write("\n//" + command + "\n\n");
    }
    private void writeInfiniteLoopInTheEndOfAsmFile(){
        writer.write("(END_OF_THE_PROGRAM)\n@END_OF_THE_PROGRAM\n0;JMP");
    }
    public void close(){
        writeInfiniteLoopInTheEndOfAsmFile();
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void writePushPop(int command, String segment, int index){
        String commandString;
        switch (command){
            case Parser.C_POP -> commandString = "pop";
            case Parser.C_PUSH -> commandString = "push";
            default -> throw new RuntimeException();
        }
        writeRawVMCommandAsAComment(commandString + " " + segment + " " +  index);
        if(command == Parser.C_PUSH){
            switch (segment) {
                case "argument", "local", "this", "that" -> {
                    writeIntegerIntoDRegister(index);
                    writer.write("@" + vmSegmentNamesToAsmRegisters.get(segment) + "\nA=A+D\nD=M\n");
                }
                case "pointer" -> {
                    if (index == 0) {
                        writeVariableValueIntoDRegister("THIS");
                    } else if (index == 1) {
                        writeVariableValueIntoDRegister("THAT");
                    }
                }
                case "temp" -> {
                    writeIntegerIntoDRegister(index);
                    writer.write("@5\nA=A+D\nD=M\n" );
                }
                case "constant" -> writeIntegerIntoDRegister(index);
                case "static" -> writeVariableValueIntoDRegister(fileNameWithOutExt + "." + index);
            }
            writeDRegisterValueToStack();
            incrementSP();
        }else {
            decrementSP();
            switch (segment) {
                case "argument","local","this","that" -> {
                    //R13 = RAM[SP];
                    assignVariableToArgPointedBySP("R13");
                    // R14 = SEGMENT_BASE + index
                    writer.write("@" + index + "\nD=A\n@" + vmSegmentNamesToAsmRegisters.get(segment) + "\nA=A+D\nD=A\n@" + "R14" + "\nM=D\n");
                    //RAM[R14] = R13
                    writer.write("@R13\nD=M\n@R14\nA=M\nM=D\n");
                }
                case "pointer" ->{
                    if(index == 0){
                        assignVariableToArgPointedBySP("THIS");
                    }else if(index == 1){
                        assignVariableToArgPointedBySP("THAT");
                    }
                }
                case  "temp" -> {
                    //R13 = RAM[SP]
                    assignVariableToArgPointedBySP("R13");
                    //R14 = 5 + index
                    writer.write("@" + index + "\nD=A\n@" + 5 + "\nA=A+D\nD=A\n@" + "R14" + "\nM=D\n");
                    //RAM[R14] = R13
                    writer.write("@R13\nD=M\n@R14\nA=M\nM=D\n");
                }
                case "static" -> assignVariableToArgPointedBySP(fileNameWithOutExt + "." + index);
            }
        }

    }
    private void assignVariableToArgPointedBySP(String varAsm){
        writer.write("@SP\nA=M\nD=M\n@" + varAsm +"\nM=D\n");
    }

    private void writeVariableValueIntoDRegister(String baseRegister){
        writer.write("@" + baseRegister + "\nD=M\n");
    }

    private void writeDRegisterValueToStack(){
        writer.write("@SP\nA=M\nM=D\n");
    }

    private void writeIntegerIntoDRegister(int integer){
        //D = index
        writer.write("@" + integer + "\nD=A\n");
    }
    private void incrementSP(){
        //SP++
        writer.write("@SP\nM=M+1\n");
    }

    public void writeArithmetic(String command){
        labelDeclarationSequenceNumber++;
        String firstArg = "R13";
        String secondArg = "R14";
        String endOfBlockLabel = "END_OF_BLOCK";
        writeRawVMCommandAsAComment(command);
        switch (command) {
            case "add" -> writeVMAddCommand(firstArg, secondArg);
            case "sub" -> writeVMSubCommand(firstArg, secondArg);
            case "neg" -> writeVMNegCommand(firstArg);
            case "eq", "gt", "lt" -> writeVMComparisonCommand(command, firstArg, secondArg, endOfBlockLabel);
            case "and", "or" -> writeVMAndorORCommand(command, firstArg, secondArg);
            case "not" -> writeVMNotCommand(firstArg);
        }
    }
    private void writeVMAddCommand(String firstArg, String secondArg){
        //VM command "add" translated to hack assembly code
        //firstArg = SP - 1
        getStackFirstElementPointerAndWriteInVariable(firstArg);
        //secondArg = SP - 2
        getStackSecondElementPointerAndWriteInVariable(secondArg);
        //sum = RAM[firstArg] + RAM[secondArg]
        writer.write("@"+ firstArg + "\nA=M\nD=M\n@" + secondArg + "\nA=M\nD=D+M\n");
        //RAM[secondArg] = sumOFFirstAndSecond
        writer.write("@" + secondArg + "\nA=M\nM=D\n");
        //SP--
        decrementSP();

    }
    private void getStackFirstElementPointerAndWriteInVariable(String firstArg){
        //assemblyVariableName = SP - 1
        writer.write("@SP\nD=M-1\n@" + firstArg + "\nM=D\n");
    }
    private void getStackSecondElementPointerAndWriteInVariable(String secondArg){
        //assemblyVariableName = SP - 2
        writer.write("@SP\nD=M-1\nD=D-1\n@"+ secondArg +"\nM=D\n");
    }
    private void decrementSP(){
        //SP--
        writer.write("@SP\nM=M-1\n");
    }

    private void writeVMSubCommand(String firstArg,String secondArg){
        getStackFirstElementPointerAndWriteInVariable(firstArg);
        getStackSecondElementPointerAndWriteInVariable(secondArg);
        //diff = RAM[second] - RAM[first]
        writer.write("@" + secondArg + "\nA=M\nD=M\n@" + firstArg +"\nA=M\nD=D-M\n");
        //RAM[second] = diff
        writer.write("@" + secondArg + "\nA=M\nM=D\n");
        decrementSP();
    }

    private void writeVMNegCommand(String firstArg){
        getStackFirstElementPointerAndWriteInVariable(firstArg);
        //RAM[firstArg] = - RAM[firstArg]
        writer.write("@" + firstArg +"\nA=M\nM=-M\n");
    }
    private void writeVMComparisonCommand(String compOperator, String firstArg,String secondArg, String endOfBlockLabel){
        String trueCondLabel;
        String trueCondJump;
        switch (compOperator) {
            case "eq" -> {
                trueCondLabel = "IS_EQUAL";
                trueCondJump = "JEQ";
            }
            case "gt" -> {
                trueCondLabel = "IS_GREATER";
                trueCondJump = "JGT";
            }
            case "lt" -> {
                trueCondLabel = "IS_LESS";
                trueCondJump = "JLT";
            }
            default -> throw new RuntimeException();
        }
        getStackFirstElementPointerAndWriteInVariable(firstArg);
        //secondArg = SP - 2
        getStackSecondElementPointerAndWriteInVariable(secondArg);
        //if (RAM[secondArg] > RAM[firstArg]) goto IS_GREATER
        subOfSecondArgFirstArgInDRegister(secondArg,firstArg);
        writer.write("@" + trueCondLabel + labelDeclarationSequenceNumber + "\nD;" + trueCondJump + "\n");
        //RAM[sec] = false
        writeStackSecondArgTrueOrFalse(secondArg,"false");
        writer.write("\n@" + endOfBlockLabel + labelDeclarationSequenceNumber + "\n0;JMP\n");
        writer.write("(" + trueCondLabel + labelDeclarationSequenceNumber + ")\n");
        writeStackSecondArgTrueOrFalse(secondArg,"true");
        writer.write("\n(" + endOfBlockLabel + labelDeclarationSequenceNumber + ")\n");
        decrementSP();

    }
    private void writeStackSecondArgTrueOrFalse(String secondArg, String booleanVal){
        if (booleanVal.equals("true")){
            booleanVal = "-1";
        }else if(booleanVal.equals("false")){
            booleanVal = "0";
        }
        writer.write("@" + secondArg + "\nA=M\nM=" + booleanVal + "\n");
    }

    private void subOfSecondArgFirstArgInDRegister(String secondArg, String firstArg){
        writer.write("@" + secondArg + "\nA=M\nD=M\n@" + firstArg + "\nA=M\nD=D-M\n");
    }

    private void writeVMAndorORCommand(String command, String firstArg, String secondArg){
        String operator = switch (command){
            case "and":
                yield "&";
            case "or":
                yield "|";
            default:
                throw new RuntimeException();
        };
        getStackFirstElementPointerAndWriteInVariable(firstArg);
        getStackSecondElementPointerAndWriteInVariable(secondArg);
        writer.write("@" + firstArg + "\nA=M\nD=M\n@" + secondArg + "\nA=M\nD=D" + operator +"M\n");
        writer.write("@" + secondArg + "\nA=M\nM=D\n");
        decrementSP();
    }

    private void writeVMNotCommand(String firstArg){
        getStackFirstElementPointerAndWriteInVariable(firstArg);
        writer.write("@" + firstArg + "\nA=M\nM=!M\n");
    }
}
