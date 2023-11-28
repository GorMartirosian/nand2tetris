package main.compiler.syntaxanalysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {

    JackTokenizer tokenizer;
    CodeBufferedWriter writer;

    public CompilationEngine(File inputFile, File outputFile){
        this.tokenizer = new JackTokenizer(inputFile);
        try {
            writer = new CodeBufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompilationEngine(String inputFileName, String outputFileName){
        this.tokenizer = new JackTokenizer(inputFileName);
        try {
            writer = new CodeBufferedWriter(new FileWriter(outputFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void compileClass(){
        writer.write("<class>\n");

        process("class");

        //processes class name
        process();

        process("{");

        while (tokenizer.currentToken.equals("static") || tokenizer.currentToken.equals("field")){
            compileClassVarDec();
        }

        while (tokenizer.currentToken.equals("constructor") || tokenizer.currentToken.equals("function")
                || tokenizer.currentToken.equals("method")){
            compileSubroutine();
        }

        process("}");
        writer.write("</class>\n");

    }

    public void compileClassVarDec(){
        writer.write("<classVarDec> ");

        if(tokenizer.currentToken.equals("static")){
            process("static");
        }else if(tokenizer.currentToken.equals("field")){
            process("field");
        }else {
            throw new RuntimeException("Syntax error!");
        }

        //process return type
        processReturnType();
        //process variable name
        process();

        while (tokenizer.currentToken.equals(",")){

            //process symbol ","
            process(",");

            //process variable name
            process();

        }

        process(";");

        writer.write("</classVarDec>\n");

    }
    private void processReturnType(){
        if(tokenizer.currentToken.equals("int")){
            process("int");
        }else if(tokenizer.currentToken.equals("char")){
            process("char");
        }else if(tokenizer.currentToken.equals("boolean")){
            process("boolean");
        }else if(tokenizer.currentTokenType == JackTokenizer.IDENTIFIER){
            //process return class type
            process();
        }else {
            throw new RuntimeException("Syntax error!");
        }
    }

    public void compileSubroutine(){
        writer.write("<subroutineDec> ");

        switch (tokenizer.currentToken) {
            case "constructor" -> process("constructor");
            case "function" -> process("function");
            case "method" -> process("method");
            default -> throw new RuntimeException("Syntax error");
        }

        if(tokenizer.currentToken.equals("void")){
            process("void");
        }else{
            //process subroutine return type
            process();
        }

        //process subroutine name
        process();

        process("(");

        compileParameterList();

        process(")");

        compileSubroutineBody();

        writer.write("</subroutineDec>\n");
    }

    public void compileParameterList(){
         writer.write("<parameterList> ");
         if(tokenizer.currentToken.equals("int") || tokenizer.currentToken.equals("char") ||
                 tokenizer.currentToken.equals("boolean") || tokenizer.currentTokenType == JackTokenizer.IDENTIFIER){
             processReturnType();
             // process variable name
             process();

             while (tokenizer.currentToken.equals(",")){
                 process(",");
                 if(tokenizer.currentToken.equals("int") || tokenizer.currentToken.equals("char") ||
                         tokenizer.currentToken.equals("boolean") || tokenizer.currentTokenType == JackTokenizer.IDENTIFIER){
                     processReturnType();
                 }

                 //process var name
                 process();
             }
         }

         writer.write("</parameterList>\n");
    }

    private void process(String langElement){
        if(tokenizer.currentToken.equals(langElement)){
            writeXMLToken();
            if(tokenizer.hasMoreTokens()){
                tokenizer.advance();
            }
        }
        else {
            throw new RuntimeException("Syntax Error!");
        }
    }

    private void process(){
        writeXMLToken();
        if(tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }

    public void compileSubroutineBody(){
        writer.write("<subroutineBody> ");

        process("{");
        while (tokenizer.currentToken.equals("var")){
            compileVarDec();
        }

        compileStatements();

        process("}");
        writer.write("</subroutineBody>\n");
    }

    public void compileVarDec(){
        writer.write("<varDec> ");
        process("var");

        processReturnType();

        //process var name
        process();

        while (tokenizer.currentToken.equals(",")){
            process(",");
             //process var name
            process();

        }

        process(";");

        writer.write("</varDec>\n");
    }

    public void compileStatements(){
        writer.write("<statements> ");

        while (tokenizer.currentToken.equals("let") || tokenizer.currentToken.equals("if") ||
                tokenizer.currentToken.equals("while") || tokenizer.currentToken.equals("do") ||
                tokenizer.currentToken.equals("return")){
            switch (tokenizer.currentToken){
                case "let" -> compileLet();
                case "if" -> compileIf();
                case "while" -> compileWhile();
                case "do" -> compileDo();
                case "return" -> compileReturn();
            }
        }
        writer.write("</statements>\n");
    }

    public void compileReturn(){
        writer.write("<returnStatement> ");
        process("return");
        if(!tokenizer.currentToken.equals(";")){
            compileExpression();
        }
        process(";");
        writer.write("</returnStatement>\n");
    }

    public void compileDo(){
        writer.write("<doStatement> ");
        process("do");

        compileSubroutineCall();

        process(";");
        writer.write("</doStatement>\n");

    }

    private void compileSubroutineCall(){
        //advance over subName o or className or varName
        process();

        if(tokenizer.currentToken.equals("(")){
            process("(");

            compileExpressionList();
            process(")");
        }else if(tokenizer.currentToken.equals(".")){
            process(".");

            //process subName
            process();

            process("(");
            compileExpressionList();
            process(")");

        }else {
            throw new RuntimeException();
        }
    }
    public void compileWhile(){
        writer.write("<whileStatement> ");
        process("while");
        process("(");
        compileExpression();
        process(")");
        process("{");
        compileStatements();
        process("}");
        writer.write("</whileStatement>\n");
    }

    public void compileIf(){
        writer.write("<ifStatement> ");
        process("if");
        process("(");
        compileExpression();
        process(")");
        process("{");
        compileStatements();
        process("}");

        if(tokenizer.currentToken.equals("else")){
            process("else");
            process("{");
            compileStatements();
            process("}");
        }
        writer.write("</ifStatement>\n");
    }

    public void compileLet(){
        writer.write("<letStatement> ");
        process("let");
        //process var name
        process();
        if(tokenizer.currentToken.equals("[")){
            process("[");

            compileExpression();

            process("]");

        }

        process("=");
        compileExpression();
        process(";");
        writer.write("</letStatement>\n");
    }
    public void compileExpression(){
        writer.write("<expression> ");

        compileTerm();

        boolean isOperator = currentTokenIsOperator();

        while (isOperator){
            //process operator
            process();

            compileTerm();

            isOperator = currentTokenIsOperator();
        }

        writer.write("</expression>\n");
    }

    public void compileTerm(){
        writer.write("<term>");
        if(tokenizer.currentTokenType == JackTokenizer.INT_CONST ||
        tokenizer.currentTokenType == JackTokenizer.STRING_CONST ||
        tokenizer.currentTokenType == JackTokenizer.KEYWORD){
            process();
        }else if(tokenizer.currentTokenType == JackTokenizer.IDENTIFIER){
            String nextToken = tokenizer.nextTokenLookahead();
            if(nextToken.equals("[")){
                //process var name
                process();
                process("[");
                compileExpression();
                process("]");
            }else if(nextToken.equals("(") || nextToken.equals(".")){
                compileSubroutineCall();
            }else {
                //process variable
                process();
            }
        } else if (tokenizer.currentToken.equals("(")) {
            process("(");
            compileExpression();
            process(")");
        } else if(tokenizer.currentToken.equals("~") || tokenizer.currentToken.equals("-")){
            process();
            compileTerm();
        }else {
            throw new RuntimeException();
        }
        writer.write("</term>");
    }
    public int compileExpressionList(){
        writer.write("<expressionList> ");
        int exprCount = 0;
        if(!tokenizer.currentToken.equals(")")){
            compileExpression();
            exprCount++;
            while (tokenizer.currentToken.equals(",")){
                process(",");
                compileExpression();
                exprCount++;
            }
        }
        writer.write("</expressionList>\n");
        return exprCount;
    }

    private boolean currentTokenIsOperator(){
        String[] operators = {"+","-","*","/","&","|","<",">","="};
        boolean isOperator = false;
        for(String operator: operators){
            if(tokenizer.currentToken.equals(operator)){
                isOperator = true;
                break;
            }
        }
        return isOperator;
    }

    public void writeXMLToken(){
        String tokenTypeStr;
        String tokenRepresentation = tokenizer.currentToken;
        switch (tokenizer.currentTokenType){
            case JackTokenizer.KEYWORD -> tokenTypeStr = "keyword";
            case JackTokenizer.IDENTIFIER -> tokenTypeStr = "identifier";
            case JackTokenizer.INT_CONST -> tokenTypeStr = "integerConstant";
            case JackTokenizer.STRING_CONST -> tokenTypeStr = "stringConstant";
            case JackTokenizer.SYMBOL -> tokenTypeStr = "symbol";
            default -> throw new RuntimeException();
        }
        if(tokenizer.tokenType() == JackTokenizer.SYMBOL){
            switch (tokenizer.currentToken) {
                case ">" -> tokenRepresentation = "&gt;";
                case "<" -> tokenRepresentation = "&lt;";
                case "\"" -> tokenRepresentation = "&quot;";
                case "&" -> tokenRepresentation = "&amp;";
            }
        }
        if(tokenizer.tokenType() == JackTokenizer.STRING_CONST){
            tokenRepresentation = tokenizer.stringVal();
        }
        writer.write("<" + tokenTypeStr + "> " +
                tokenRepresentation + " </" + tokenTypeStr + ">\n" );
    }

}
