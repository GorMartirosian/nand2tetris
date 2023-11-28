package main.virtualmachine;

import java.io.*;

public class Parser {
    BufferedReader reader;
    int codeLineCounter;
    String currentLine;
    String currentCommand;
    int currentCommandType;
    String[] splitCommand;
    protected static final String[] segments = {"argument", "local", "static", "constant", "this", "that", "pointer", "temp"};
    private static final String[] arithmeticCommands = {"add", "sub", "neg"};
    static final String[] comparisonCommands = {"eq", "gt", "lt"};
    private static final String[] logicalCommands = {"and", "or", "not"};
    private static final String[][] arithmeticLogicalCommands = {arithmeticCommands,comparisonCommands,logicalCommands};
    final int C_ARITHMETIC = 0;
    public final static int C_PUSH = 1;
    public final static int C_POP = 2;
    final static int C_LABEL = 3;
    final static int C_GOTO = 4;
    final static int C_IF = 5;
    final static int C_FUNCTION = 6;
    final static int C_RETURN = 7;
    final static int C_CALL = 8;

    public Parser(File inputFile){
        try {
            reader = new BufferedReader( new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        advance();
    }

    private String lineFetch(){
        String line;
        try {
            line = reader.readLine();
            if (line != null){
                return line.trim().replaceAll("\\s+"," ");
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    protected void advance(){
        currentLine = lineFetch();
        while(currentLine != null){
            if(currentLine.startsWith("//") || currentLine.equals("")){
                currentLine = lineFetch();
            }else{
                currentCommand = currentLine;
                splitCommand = currentCommand.split("\\s+");
                currentCommandType = commandType();
                codeLineCounter++;
                return;
            }
        }
    }

    private int commandType(){
        if(commandTypeC_ARITHMETIC(splitCommand)){
            return C_ARITHMETIC;
        }else if(commandTypeC_PUSH(splitCommand)){
            return C_PUSH;
        }else if(commandTypeC_POP(splitCommand)){
            return C_POP;
        }else if(commandTypeC_LABEL(splitCommand)){
            return C_LABEL;
        }else if(commandTypeC_GOTO(splitCommand)){
            return C_GOTO;
        }else if(commandTypeC_IF(splitCommand)){
            return C_IF;
        }else if(commandTypeC_FUNCTION(splitCommand)){
            return C_FUNCTION;
        }else if(commandTypeC_CALL(splitCommand)){
            return C_CALL;
        } else {
            return -1;
        }

    }
    private boolean commandTypeC_CALL(String[] splitCommand){
        return splitCommand.length == 3 && splitCommand[0].equals("call") && isNumeric(splitCommand[2]);
    }
    private boolean commandTypeC_FUNCTION(String[] splitCommand){
        return splitCommand.length == 3 && splitCommand[0].equals("function") && isNumeric(splitCommand[2]);
    }
    private boolean commandTypeC_IF(String[] splitCommand){
        return splitCommand.length == 2 && splitCommand[0].equals("if-goto");
    }
    private boolean commandTypeC_GOTO(String[] splitCommand){
        return splitCommand.length == 2 && splitCommand[0].equals("goto");
    }
    private boolean commandTypeC_LABEL(String[] splitCommand){
        if(splitCommand.length == 2 && splitCommand[0].equals("label")){
            return !isNumeric("" + splitCommand[1].charAt(0));
        }
        return false;
    }
    private boolean commandTypeC_ARITHMETIC(String[] splitCommand){
        if(splitCommand.length == 1){
            for(String[] commandsArray : arithmeticLogicalCommands){
                for (String command : commandsArray){
                    if(splitCommand[0].equals(command)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private boolean commandTypeC_PUSH(String[] splitCommand){
        if(splitCommand.length == 3 && splitCommand[0].equals("push") && isNumeric(splitCommand[2])){
            for(String segment: segments){
                if(splitCommand[1].equals(segment)){
                    return true;
                }
            }
        }
        return false;
    }
    private boolean commandTypeC_POP(String[] splitCommand){
        if(splitCommand.length == 3 && splitCommand[0].equals("pop") && isNumeric(splitCommand[2])){
            for(String segment: segments){
                if(splitCommand[1].equals(segment)){
                    return true;
                }
            }
        }
        return false;
    }
    private boolean isNumeric(String str){
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) < '0'|| str.charAt(i) > '9'){
                return false;
            }
        }
        return true;
    }

}
