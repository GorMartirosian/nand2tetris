package main.compiler.codegeneration;

import java.util.HashMap;

public class SymbolTable {

    private final HashMap<String,String[]> entries;

    private  int fieldIndex;
    private  int staticIndex;
    private  int argIndex;
    private  int varIndex;

    SymbolTable(){
        entries = new HashMap<>();
    }

    void reset(){
        entries.clear();
        fieldIndex = 0;
        staticIndex = 0;
        argIndex = 0;
        varIndex = 0;
    }

    void define(String name, String type, VariableKinds kind){
        int index;
        switch (kind){
            case ARG -> {
                index = argIndex;
                argIndex++;
            }
            case VAR -> {
                index = varIndex;
                varIndex++;
            }
            case STATIC -> {
                index = staticIndex;
                staticIndex++;
            }
            case FIELD -> {
                index = fieldIndex;
                fieldIndex++;
            }
            default -> throw new RuntimeException();
        }
        String[] values = {type,kind.segment, String.valueOf(index)};
        entries.put(name,values);
    }

    boolean containsVariable(String name){
        return entries.containsKey(name);
    }

    int varCount(VariableKinds kind){
        switch (kind){
            case ARG -> {return argIndex;}
            case VAR -> {return varIndex;}
            case FIELD -> {return fieldIndex;}
            case STATIC -> {return staticIndex;}
            default -> throw new RuntimeException();
        }
    }

    Segments segmentOf(String name){
        VariableKinds kind = kindOf(name);
        switch (kind){
            case ARG -> {return Segments.ARGUMENT;}
            case VAR -> {return Segments.LOCAL;}
            case STATIC -> {return Segments.STATIC;}
            case FIELD -> {return Segments.THIS;}
            default -> throw new RuntimeException();
        }
    }

    VariableKinds kindOf(String name){
        String[] values = entries.get(name);
        switch (values[1]){
            case "local" ->{return VariableKinds.VAR;}
            case "argument" -> {return VariableKinds.ARG;}
            case "this" -> {return VariableKinds.FIELD;}
            case "static" -> {return VariableKinds.STATIC;}
            default -> throw new RuntimeException();
        }
    }

    String typeOf(String name){
        return entries.get(name)[0];
    }

    int indexOf(String name){
        return Integer.parseInt(entries.get(name)[2]);
    }

}

