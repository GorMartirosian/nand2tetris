package main.assembler;

import java.util.Arrays;
import java.util.HashMap;

public class SymbolTable {
    //constructor must create empty hash table that will later contain symbols and mapped addresses
    HashMap<String,Integer> symbolTable;
    int currentAvailableMemoryPointerForVariable;
    private final String[] finalMaps;
    {
        String[] labels = {"SP","LCL","ARG","THIS","THAT","SCREEN","KBD"};
        finalMaps = Arrays.copyOf(labels,23);
        for(int i = 7; i < finalMaps.length; i++){
            finalMaps[i] = "R" + (i-7);
        }
    }
    public SymbolTable(){
        symbolTable = new HashMap<>();
        for(int i = 0; i <= 15; i++){
            symbolTable.put("R" + i,i);
        }
        symbolTable.put("SP",0);
        symbolTable.put("LCL",1);
        symbolTable.put("ARG",2);
        symbolTable.put("THIS",3);
        symbolTable.put("THAT",4);
        symbolTable.put("SCREEN",16384);
        symbolTable.put("KBD", 24576);
        currentAvailableMemoryPointerForVariable = 16;
    }
    protected void addEntry(String symbol, int address){
        for (String val: finalMaps){
            if(symbol.equals(val)){
                throw new RuntimeException();
            }
        }
        if(address < 0 || address > 32767){
            throw new RuntimeException();
        }
        symbolTable.put(symbol,address);
    }
    protected boolean contains(String symbol){
        return symbolTable.containsKey(symbol);
    }
    protected int getAddress(String symbol){
        if(symbolTable.containsKey(symbol)){
            return symbolTable.get(symbol);
        }
        throw new RuntimeException();
    }
}
