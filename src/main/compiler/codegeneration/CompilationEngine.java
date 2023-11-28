package main.compiler.codegeneration;

import java.io.File;
public class CompilationEngine {

    private final JackTokenizer tokenizer;
    private final VMWriter writer;
    private String className;
    private int labelRunningNumber;
    private String currentSubroutineName;
    private boolean isMethod;
    private boolean isConstructor;
    private boolean isFunction;
    private final SymbolTable classSymbolTable;
    private final SymbolTable subroutineSymbolTable;

    public CompilationEngine(File inputFile, File outputFile){
        tokenizer = new JackTokenizer(inputFile);
        writer = new VMWriter(outputFile);
        classSymbolTable = new SymbolTable();
        subroutineSymbolTable = new SymbolTable();
    }


    public void compileClass(){


        process("class");

        className = tokenizer.currentToken;

        //processes class name
        process();

        process("{");

        while (tokenizer.currentToken.equals("static") || tokenizer.currentToken.equals("field")){
            compileClassVarDec();
        }

        while (tokenizer.currentToken.equals("constructor") || tokenizer.currentToken.equals("function")
                || tokenizer.currentToken.equals("method")){
            isMethod = false;
            isConstructor = false;
            isFunction = false;
            compileSubroutine();
        }

        process("}");
        writer.close();
    }

    private void compileClassVarDec(){

        boolean isField = false;
        if(tokenizer.currentToken.equals("static")){
            process("static");

        }else if(tokenizer.currentToken.equals("field")){
            process("field");
            isField = true;
        }else {
            return;
        }

        String returnType = tokenizer.currentToken;
        //process return type
        process();
        String varName = tokenizer.currentToken;
        //process variable name
        process();

        VariableKinds kind;
        if(isField){
            kind = VariableKinds.FIELD;
        }else {
            kind = VariableKinds.STATIC;
        }
        classSymbolTable.define(varName,returnType,kind);

        while (tokenizer.currentToken.equals(",")){

            //process symbol ","
            process(",");

            varName = tokenizer.currentToken;
            classSymbolTable.define(varName,returnType,kind);
            //process variable name
            process();
        }
        process(";");
    }


    private void compileSubroutine(){

        subroutineSymbolTable.reset();

        switch (tokenizer.currentToken) {
            case "constructor" -> {
                process("constructor");
                isConstructor = true;
            }
            case "function" -> {
                isFunction = true;
                process("function");
            }
            case "method" -> {
                subroutineSymbolTable.define("this",className, VariableKinds.ARG);
                isMethod = true;
                process("method");}
            default -> throw new RuntimeException("Syntax error");
        }

        //process subroutine return type
        process();

        currentSubroutineName = tokenizer.currentToken;
        //process subroutine name
        process();

        process("(");

        compileParameterList();

        process(")");



        compileSubroutineBody();

    }
    private void compileParameterList(){
         if(tokenizer.currentToken.equals("int") || tokenizer.currentToken.equals("char") ||
                 tokenizer.currentToken.equals("boolean") || tokenizer.currentTokenType == JackTokenizer.IDENTIFIER){

             String type = tokenizer.currentToken;
             //process type
             process();

             String name = tokenizer.currentToken;
             // process variable name
             process();

             subroutineSymbolTable.define(name,type, VariableKinds.ARG);
             while (tokenizer.currentToken.equals(",")){
                 process(",");

                 type = tokenizer.currentToken;
                 process();


                 name = tokenizer.currentToken;
                 process();

                 subroutineSymbolTable.define(name,type, VariableKinds.ARG);
             }
         }

    }

    private void process(String langElement){
        if(tokenizer.currentToken.equals(langElement)){
            if(tokenizer.hasMoreTokens()){
                tokenizer.advance();
            }
        }
        else {
            throw new RuntimeException("Syntax Error!");
        }
    }

    private void process(){
        if(tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }

    private void compileSubroutineBody(){

        process("{");
        while (tokenizer.currentToken.equals("var")){
            compileVarDec();
        }

        writer.writeFunction(className + "." + currentSubroutineName,subroutineSymbolTable.varCount(VariableKinds.VAR) );
        if(isConstructor){
            writer.writePush(Segments.CONSTANT,classSymbolTable.varCount(VariableKinds.FIELD));
            writer.writeCall("Memory.alloc",1);
            writer.writePop(Segments.POINTER,0);
        }else if(isMethod){
            writer.writePush(Segments.ARGUMENT,0);
            writer.writePop(Segments.POINTER,0);
        }

        compileStatements();

        process("}");
    }

    private void compileVarDec(){
        process("var");

        String type = tokenizer.currentToken;
        process();

        String name = tokenizer.currentToken;
        process();

        subroutineSymbolTable.define(name,type, VariableKinds.VAR);
        while (tokenizer.currentToken.equals(",")){
            process(",");

            name = tokenizer.currentToken;
            process();
            subroutineSymbolTable.define(name,type, VariableKinds.VAR);
        }

        process(";");

    }

    private void compileStatements(){

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
    }

    private void compileReturn(){
        process("return");
        if(isConstructor){
            process("this");
            process(";");
            writer.writePush(Segments.POINTER,0);
            writer.writeReturn();
            return;
        }
        if(!tokenizer.currentToken.equals(";")){
            compileExpression();
            writer.writeReturn();
        }else {
            //is a void method/function
            writer.writePush(Segments.CONSTANT,0);
            writer.writeReturn();
        }
        process(";");
    }

    private void compileDo(){
        process("do");

        compileSubroutineCall();
        writer.writePop(Segments.TEMP,0);
        process(";");

    }

    private void compileSubroutineCall(){

        String subroutineCaller;
        String subroutineName;
        String nextToken = tokenizer.nextTokenLookahead();
        if(nextToken.equals(".")){
            subroutineCaller = tokenizer.currentToken;
            process();
            process(".");
            subroutineName = tokenizer.currentToken;
            process();
        }else if(nextToken.equals("(")){
            subroutineName = tokenizer.currentToken;
            subroutineCaller = "this";
            process();
        } else {
            throw new RuntimeException();
        }

        process("(");

        if(subroutineSymbolTable.containsVariable(subroutineCaller)){
            writer.writePush(subroutineSymbolTable.segmentOf(subroutineCaller),subroutineSymbolTable.indexOf(subroutineCaller));
            int nArgs = compileExpressionList();
            writer.writeCall(subroutineSymbolTable.typeOf(subroutineCaller) + "." + subroutineName,nArgs + 1);
        }else if(classSymbolTable.containsVariable(subroutineCaller)){
            writer.writePush(classSymbolTable.segmentOf(subroutineCaller),classSymbolTable.indexOf(subroutineCaller));
            int nArgs = compileExpressionList();
            writer.writeCall(classSymbolTable.typeOf(subroutineCaller) + "." + subroutineName,nArgs + 1);
        }else if(subroutineCaller.equals("this")){
            writer.writePush(Segments.POINTER,0);
            int nArgs = compileExpressionList();
            writer.writeCall(className + "." + subroutineName,nArgs + 1);
        }else{
            //if function call
            int nArgs = compileExpressionList();
            writer.writeCall(subroutineCaller + "." + subroutineName, nArgs);
        }

        process(")");
    }
    private void compileWhile(){
        process("while");
        writer.writeLabel("LABEL_" + labelRunningNumber);

        process("(");
        compileExpression();
        process(")");

        writer.writeArithmetic(Commands.NOT);
        writer.writeIf("LABEL_" + (labelRunningNumber + 1));

        process("{");
        compileStatements();
        process("}");

        writer.writeGoto("LABEL_" + labelRunningNumber);
        writer.writeLabel("LABEL_" + (labelRunningNumber + 1));

        labelRunningNumber += 2;
    }

    private void compileIf(){
        process("if");
        process("(");
        compileExpression();
        process(")");

        writer.writeArithmetic(Commands.NOT);
        writer.writeIf("LABEL_" + labelRunningNumber);

        process("{");
        compileStatements();
        process("}");

        if(tokenizer.currentToken.equals("else")){

            process("else");

            writer.writeGoto("LABEL_" + (labelRunningNumber + 1));
            writer.writeLabel("LABEL_" + labelRunningNumber);
            process("{");
            compileStatements();
            process("}");
            writer.writeLabel("LABEL_" + (labelRunningNumber + 1));
        }else {
            writer.writeLabel("LABEL_" + labelRunningNumber);
        }
        labelRunningNumber += 2;
    }

    private void compileLet(){

        process("let");

        //process var name
        String varName = tokenizer.currentToken;
        process();
        int index;
        boolean inSubroutineTable = false;

        if(subroutineSymbolTable.containsVariable(varName)){
            index = subroutineSymbolTable.indexOf(varName);
            inSubroutineTable = true;
        }else {
            index = classSymbolTable.indexOf(varName);
        }

        if(tokenizer.currentToken.equals("[")){
            if(inSubroutineTable){
                writer.writePush(subroutineSymbolTable.segmentOf(varName),index);
            }else {
                writer.writePush(classSymbolTable.segmentOf(varName),index);
            }

            process("[");
            compileExpression();
            process("]");

            writer.writeArithmetic(Commands.ADD);

            process("=");
            compileExpression();

            writer.writePop(Segments.TEMP,0);
            writer.writePop(Segments.POINTER,1);
            writer.writePush(Segments.TEMP,0);
            writer.writePop(Segments.THAT,0);
        }else {
            process("=");
            compileExpression();

            if(inSubroutineTable){
                writer.writePop(subroutineSymbolTable.segmentOf(varName),index);
            }else {
                writer.writePop(classSymbolTable.segmentOf(varName),index);
            }
        }

        process(";");

    }
    private void compileExpression(){

        compileTerm();

        boolean isOperator = currentTokenIsOperator();

        while (isOperator){
            String operator = tokenizer.currentToken;
            //process operator
            process();

            compileTerm();

            switch (operator){
                case "+" -> writer.writeArithmetic(Commands.ADD);
                case "-" -> writer.writeArithmetic(Commands.SUB);
                case "*" -> writer.writeCall("Math.multiply",2);
                case "/" -> writer.writeCall("Math.divide",2);
                case "&" -> writer.writeArithmetic(Commands.AND);
                case "|" -> writer.writeArithmetic(Commands.OR);
                case "<" -> writer.writeArithmetic(Commands.LT);
                case ">" -> writer.writeArithmetic(Commands.GT);
                case "=" -> writer.writeArithmetic(Commands.EQ);
            }
            isOperator = currentTokenIsOperator();
        }

    }


    private void compileTerm(){
        if(tokenizer.currentTokenType == JackTokenizer.INT_CONST){
            writer.writePush(Segments.CONSTANT,tokenizer.intVal());
            process();
        }else if( tokenizer.currentTokenType == JackTokenizer.STRING_CONST){
            String stringLiteral = tokenizer.stringVal();
            writer.writePush(Segments.CONSTANT,stringLiteral.length());
            writer.writeCall("String.new",1);
            for(int i = 0; i < stringLiteral.length(); i++){
                int charCode = stringLiteral.charAt(i);
                writer.writePush(Segments.CONSTANT, charCode);
                writer.writeCall("String.appendChar",1);
            }
            process();
        }else if(tokenizer.currentTokenType == JackTokenizer.KEYWORD){
            switch (tokenizer.currentToken) {
                case "false", "null" -> writer.writePush(Segments.CONSTANT, 0);
                case "true" -> {
                    writer.writePush(Segments.CONSTANT, 1);
                    writer.writeArithmetic(Commands.NEG);
                }
                case "this" -> writer.writePush(Segments.POINTER,0);
            }
            process();
        }else if(tokenizer.currentTokenType == JackTokenizer.IDENTIFIER){
            String nextToken = tokenizer.nextTokenLookahead();
            if(nextToken.equals("[")){
                //process var name
                String arrName = tokenizer.currentToken;
                if(subroutineSymbolTable.containsVariable(arrName)){
                    writer.writePush(subroutineSymbolTable.segmentOf(arrName),subroutineSymbolTable.indexOf(arrName));
                }else {
                    //else it is in classSymTable
                    writer.writePush(classSymbolTable.segmentOf(arrName),classSymbolTable.indexOf(arrName));
                }
                process();
                process("[");
                compileExpression();
                process("]");
                writer.writeArithmetic(Commands.ADD);
                writer.writePop(Segments.POINTER,1);
                writer.writePush(Segments.THAT,0);
            }else if(nextToken.equals("(") || nextToken.equals(".")){
                compileSubroutineCall();
            }else {
                //process variable
                String variable = tokenizer.currentToken;
                if(subroutineSymbolTable.containsVariable(variable)){
                    writer.writePush(subroutineSymbolTable.segmentOf(variable),subroutineSymbolTable.indexOf(variable));
                }else {
                    //else current variable is in classSymTable
                    writer.writePush(classSymbolTable.segmentOf(variable),classSymbolTable.indexOf(variable));
                }
                process();
            }
        } else if (tokenizer.currentToken.equals("(")) {
            process("(");
            compileExpression();
            process(")");
        } else if(tokenizer.currentToken.equals("~") || tokenizer.currentToken.equals("-")){
            String unaryOp = tokenizer.currentToken;
            process();
            compileTerm();
            switch (unaryOp) {
                case "~" -> writer.writeArithmetic(Commands.NOT);
                case "-" -> writer.writeArithmetic(Commands.NEG);
            }
        }else {
            throw new RuntimeException();
        }
    }
    private int compileExpressionList(){
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

}
