package main.compiler.syntaxanalysis;

public class CompileTimeException extends RuntimeException{
    public CompileTimeException(){
        super("A compile error occurred!");
    }
    public CompileTimeException(String description){
        super(description);
    }
}