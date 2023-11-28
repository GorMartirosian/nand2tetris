package main.assembler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import custom_exceptions.CompileTimeException;
public class Parser {
    BufferedReader reader;
    String currentInstruction;
    int currentInstructionType;
    String currentLine;
    final int A_INSTRUCTION = 0;
    final int C_INSTRUCTION = 1;
    final int L_INSTRUCTION = 2;
    public Parser(String filename) {
        try {
            reader = new BufferedReader(new FileReader(filename));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
    //returns part of the current instruction with destination saving place: for 'D = D + M', returns 'D'
    public String dest(){
        if(currentInstructionType == C_INSTRUCTION){
            if (currentInstruction.contains("=")) {
                int endIndexOfDestPart = currentInstruction.indexOf("=");
                return currentInstruction.substring(0, endIndexOfDestPart);
            }else {
                return "null";
            }
        }else {
            throw new RuntimeException();
        }
    }
    //returns the computation part of the current instruction: for 'D = D + M', returns 'D + M'
    public String comp(){
        if(currentInstructionType == C_INSTRUCTION){
            if(currentInstruction.contains("=") && currentInstruction.contains(";")){
                int startIndexOfCompPart = currentInstruction.indexOf("=") + 1;
                int endIndexOfCompPart = currentInstruction.indexOf(";");
                return currentInstruction.substring(startIndexOfCompPart,endIndexOfCompPart);
            }else if( currentInstruction.contains("=")){
                int startIndexOfCompPart = currentInstruction.indexOf("=") + 1;
                return currentInstruction.substring(startIndexOfCompPart);
            }else if( currentInstruction.contains(";")){
                int endIndexOfCompPart = currentInstruction.indexOf(";");
                return currentInstruction.substring(0,endIndexOfCompPart);
            }else{
                return currentInstruction;
            }
        }else{
            throw new RuntimeException();
        }
    }
    //returns the destination jump part of the instruction: for 'D = D + M; JLE' returns 'JLE'.
    public String jump(){
        if(currentInstructionType == C_INSTRUCTION){
            if (currentInstruction.contains(";")) {
                int indexOfFirstChar = currentInstruction.indexOf(";") + 1;
                return currentInstruction.substring(indexOfFirstChar).trim();
            }else{
                return "null";
            }
        } else {
            throw new RuntimeException();
        }
    }
    //returns the symbol in current instruction: for '@17' -> '17', '@sum' -> 'sum', "(LOOP)" -> 'LOOP'
    public String symbol(){
        if(currentInstructionType == A_INSTRUCTION){
            return currentInstruction.substring(1);
        } else if (currentInstructionType == L_INSTRUCTION){
            return currentInstruction.substring(1,currentInstruction.length() - 1);
        } else{
            throw new RuntimeException();
        }

    }
    //returns A_INSTRUCTION< C_INSTRUCTION< L_INSTRUCTION for
    //@xxx (xxx is decimal number or symbol),
    //dest=comp;jump and
    //(xxx) (xxx are symbol(ex. LOOP)) respectively
    public int instructionType(){
        if(currentInstruction.startsWith("@")){
            checkAInstructionValidity();
            return A_INSTRUCTION;
        }else if(currentInstruction.startsWith("(") && currentInstruction.endsWith(")")){
            return L_INSTRUCTION;
        }else{
            return C_INSTRUCTION;
        }
    }
    private void checkAInstructionValidity(){
        if(currentInstruction.charAt(1) >= '0' && currentInstruction.charAt(1) <= '9' &&
                currentInstruction.length() > 2){
            for(int i = 2; i< currentInstruction.length(); i++){
                if( currentInstruction.charAt(i) < '0' || currentInstruction.charAt(i) > '9'){
                    throw new CompileTimeException("A variable can't start with a digit!");
                }
            }
        }
    }

    public void advance() {
        currentLine = lineFetch();
        while (currentLine != null){
            if(!currentLine.startsWith("//") && !currentLine.equals("")){
                currentInstruction = currentLine.replaceAll("\\s","");
                currentInstructionType = instructionType();
                return;
            }
            currentLine = lineFetch();
        }
    }
    private String lineFetch(){
        String instruction;
        try{
            instruction = reader.readLine();
            if (instruction != null){
                return instruction.trim();
            }
            return null;
        }catch (IOException e){
            throw new RuntimeException();
        }
    }

    public static boolean startsNotWithNumber(String str){
        return str.charAt(0) < '0' || str.charAt(0) > '9';
    }
}
