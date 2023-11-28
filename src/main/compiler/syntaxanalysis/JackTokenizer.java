package main.compiler.syntaxanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class JackTokenizer {
    CodeBufferedReader reader;

    public static final String[] symbols = {"{" , "}" , "(" , ")" , "[" , "]" , "." ,
            "," , ";" , "+" , "-" , "*" , "/" , "&" , "|" , "<" , ">" , "=" , "~"};

    public static final String[] keywords = {"class", "constructor","function",
            "method","field","static","var","int","char","boolean","void",
            "true","false","null","this","let",
            "do","if","else","while","return"};

    String currentToken;
    int currentTokenType;

    static final int KEYWORD = 0;
    static final int SYMBOL = 1;
    static final int IDENTIFIER = 2;
    static final int INT_CONST = 3;
    static final int STRING_CONST = 4;
    private static final int CLASS = 5;
    private static final int METHOD = 6;
    private static final int FUNCTION = 7;
    private static final int CONSTRUCTOR = 8;
    private static final int INT = 9;
    private static final int BOOLEAN = 10;
    private static final int CHAR = 11;
    private static final int VOID = 12;
    private static final int VAR = 13;
    private static final int STATIC = 14;
    private static final int FIELD = 15;
    private static final int LET = 16;
    private static final int DO = 17;
    private static final int IF = 18;
    private static final int ELSE = 19;
    private static final int WHILE = 20;
    private static final int RETURN = 21;
    private static final int TRUE = 22;
    private static final int FALSE = 23;
    private static final int NULL = 24;
    private static final int THIS = 25;


    public JackTokenizer(File inputFile){
        try {
            reader = new CodeBufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public JackTokenizer(String inputFile){
        this(new File(inputFile));
    }

    public boolean hasMoreTokens(){
        passCommentsAndWhitespaces();
        reader.mark(8192);
        char testForEOF = reader.readChar();
        reader.reset();
        return testForEOF != (char) -1;
    }


    /** Modifies reader, currentToken and currentTokenType */
    public void advance(){
        passCommentsAndWhitespaces();
        String currentChar = Character.toString(reader.readChar());

        //if current character is symbol it is current token
        for(String symbol: symbols){
            if(currentChar.equals(symbol)){
                currentToken = currentChar;
                currentTokenType = tokenType();
                return;
            }
        }


        //if current char is not symbol, current token is current char plus other chars until delimiter; read until next delimiter
        currentToken = currentChar;

        if(currentChar.equals("\"")){
            currentChar = Character.toString(reader.readChar());
            while (!currentChar.equals("\"")){
                currentToken += currentChar;
                currentChar = Character.toString(reader.readChar());
            }
            currentToken += currentChar;
            currentTokenType = tokenType();
            return;
        }

        reader.mark(8192);
        String nextChar = Character.toString(reader.readChar());
        reader.reset();

        while (true) {
            for (String symbol : symbols) {
                if (nextChar.equals(symbol)) {
                    currentTokenType = tokenType();
                    return;
                }
            }

            if(Character.isWhitespace(nextChar.charAt(0))){
                currentTokenType = tokenType();
                return;
            }

            nextChar = Character.toString(reader.readChar());
            currentToken += nextChar;

            reader.mark(8192);
            nextChar = Character.toString(reader.readChar());
            reader.reset();
        }

    }

    public String nextTokenLookahead(){
        if(!hasMoreTokens()){
            throw new RuntimeException();
        }
        String currentTokenStr = currentToken;
        int currentTokenTypeInt = currentTokenType;
        reader.mark(8192);
        advance();
        reader.reset();
        String nextToken = currentToken;
        currentToken = currentTokenStr;
        currentTokenType = currentTokenTypeInt;
        return nextToken;
    }

    public int tokenType(){
        for(String keyword : keywords){
            if(currentToken.equals(keyword)){
                return KEYWORD;
            }
        }
        for (String symbol : symbols){
            if(currentToken.equals(symbol)){
                return SYMBOL;
            }
        }
        if(currentToken.startsWith("\"") && currentToken.endsWith("\"")){
            return STRING_CONST;
        }
        try {
            int currentTokenInt = Integer.parseInt(currentToken);
            if(currentTokenInt >= 0 && currentTokenInt <= 32767){
                return INT_CONST;
            }
        } catch (NumberFormatException e) {
            if(!Character.isDigit(currentToken.charAt(0))){
                return IDENTIFIER;
            }else {
                throw new CompileTimeException("Unexpected token.");
            }
        }
        return -1;
    }

    public int keyWord(){
        if(currentTokenType != KEYWORD){
            throw new RuntimeException();
        }
        return switch (currentToken) {
            case "class" -> CLASS;
            case "method" -> METHOD;
            case "function" -> FUNCTION;
            case "constructor" -> CONSTRUCTOR;
            case "int" -> INT;
            case "boolean" -> BOOLEAN;
            case "char" -> CHAR;
            case "void" -> VOID;
            case "var" -> VAR;
            case "static" -> STATIC;
            case "field" -> FIELD;
            case "let" -> LET;
            case "do" -> DO;
            case "if" -> IF;
            case "else" -> ELSE;
            case "while" -> WHILE;
            case "return" -> RETURN;
            case "true" -> TRUE;
            case "false" -> FALSE;
            case "null" -> NULL;
            case "this" -> THIS;
            default -> -1;
        };
    }

    public String symbol(){
        if(currentTokenType != SYMBOL){
            throw new RuntimeException();
        }
        return currentToken;
    }

    public String identifier(){
        if(currentTokenType != IDENTIFIER){
            throw new RuntimeException();
        }
        return currentToken;
    }
    public int intVal(){
        if(currentTokenType != INT_CONST){
            throw new RuntimeException();
        }
        return Integer.parseInt(currentToken);
    }

    public String stringVal(){
        if(currentTokenType != STRING_CONST){
            throw new RuntimeException();
        }
        return currentToken.substring(1,currentToken.length() - 1);
    }

    private void passCommentsAndWhitespaces(){
        boolean passedWhitespaces = passWhitespaces();
        boolean passedSingleLineComment = passSingleLineComment();
        boolean passedMultiLineComment = passMultiLineComment();

        while (passedWhitespaces || passedMultiLineComment || passedSingleLineComment){
            passedWhitespaces = passWhitespaces();
            passedSingleLineComment = passSingleLineComment();
            passedMultiLineComment = passMultiLineComment();
        }
    }

    private boolean passWhitespaces(){
        reader.mark(8192);
        char currentChar = reader.readChar();
        reader.reset();
        boolean hasPassed = false;
        while (Character.isWhitespace(currentChar)){
            hasPassed = true;

            reader.mark(8192);
            currentChar = reader.readChar();
        }
        reader.reset();
        return hasPassed;
    }

    private boolean passSingleLineComment(){
        reader.mark(8192);
        char currentChar = reader.readChar();
        char nextChar = reader.readChar();
        reader.reset();
        boolean hasPassed = false;
        if(currentChar == '/' && nextChar == '/'){
            hasPassed = true;
            reader.readLine();
        }
        return hasPassed;
    }

    private boolean passMultiLineComment(){
        reader.mark(8192);
        char currentChar = reader.readChar();
        char nextChar = reader.readChar();
        reader.reset();

        boolean hasPassed = false;

        if(currentChar == '/' && nextChar == '*'){

            //advance to comment body
            reader.readChar();
            reader.readChar();

            currentChar = reader.readChar();
            reader.mark(8192);
            nextChar = reader.readChar();
            reader.reset();
            String twoCharSeq = "" + currentChar + nextChar;

            while (!twoCharSeq.equals("*/")){
                currentChar = reader.readChar();
                reader.mark(8192);
                nextChar = reader.readChar();
                reader.reset();
                twoCharSeq = "" + currentChar + nextChar;
            }
            reader.readChar();
            hasPassed = true;
        }
        return hasPassed;
    }


    public static String[] arrayConcat(String[] first, String[] second){
        String[] result = new String[first.length + second.length];
        for(int i = 0; i < first.length; i++){
            result[i] = first[i];
        }
        for (int i = first.length; i < result.length; i++){
            result[i] = second[i - first.length];
        }
        return result;
    }

}
