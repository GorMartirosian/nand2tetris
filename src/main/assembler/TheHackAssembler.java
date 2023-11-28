package main.assembler;

import java.io.*;

public class TheHackAssembler {
    /** This is an assembler for Hack computer platform. It is used to translate the
     * assembly language to machine code.
     * The usage: type 'HackAssembler <here goes your assembly program name>.asm'
     * The output gives a binary file with .hack extension
     */

    public static void main(String[] args) {
        // args[0] is the Hack assembly language program
        //'
        if(args.length != 1){
            System.out.println("TheHackAssembler.java\nDescription: A program that translates Hack assembly language " +
                    "to Hack machine language.");
            System.out.println("Usage:java TheHackAssembler.java <here goes the fully specified assembly file path>");
            System.out.println("Output:Generates hack machine code file.");
            System.exit(0);
        }
        File inputFile = new File(args[0]);

        Parser pars = new Parser(inputFile.getAbsolutePath());
        SymbolTable table = new SymbolTable();

        //first pass to initialize the labels
        //variables @variable must be added to the table in second pass,(THINK WHY)
        pars.advance();
        int lineNumberIterator = 0;
        while (pars.currentLine != null){
            if(pars.currentInstructionType == pars.A_INSTRUCTION || pars.currentInstructionType == pars.C_INSTRUCTION){
                lineNumberIterator++;
            } else if (pars.currentInstructionType == pars.L_INSTRUCTION){
                String symbol = pars.symbol();
                if(Parser.startsNotWithNumber(symbol) && !table.contains(symbol)){
                    //not incrementing by 1 , because already done
                    table.addEntry(symbol,lineNumberIterator);
                }
            }
            pars.advance();
        }

        pars = new Parser(inputFile.getAbsolutePath());
        File outputFile = new File(inputFile.getParent() +"\\" +
                inputFile.getName().substring(0,inputFile.getName().indexOf('.'))+".hack");

        BufferedWriter writer;
        try {
            outputFile.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //second pass
        pars.advance();
        try {
            while (pars.currentLine != null) {
                if (pars.currentInstructionType == pars.A_INSTRUCTION) {
                    String symbol = pars.symbol();
                    //if variable add to the table
                    if (Parser.startsNotWithNumber(symbol) && !table.contains(symbol)) {
                        table.addEntry(symbol, table.currentAvailableMemoryPointerForVariable);
                        table.currentAvailableMemoryPointerForVariable++;
                    }
                    int decimalSymbol;
                    if (table.contains(symbol)) {
                        decimalSymbol = table.getAddress(symbol);
                    } else {
                        decimalSymbol = Integer.parseInt(symbol);
                    }
                    String binarySymbol;
                    binarySymbol = String.format("%15s", Integer.toBinaryString(decimalSymbol)).replace(' ', '0');
                    try {
                        writer.write("0" + binarySymbol);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                } else if (pars.currentInstructionType == pars.C_INSTRUCTION) {
                    try {
                        writer.write("111" + Code.comp(pars.comp()) + Code.dest(pars.dest()) + Code.jump(pars.jump()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                pars.advance();
            }
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Completed.");
    }
}