package main.assembler;

import java.util.HashMap;

public final class Code {
    private final static HashMap<String,Byte> asmToHackDest = new HashMap<>();
    private final static HashMap<String,String> asmToHackComp = new HashMap<>();
    private final static HashMap<String,Byte> asmToHackJump = new HashMap<>();
    static {
        //Destination Map
        asmToHackDest.put("null",(byte) 0b000);
        asmToHackDest.put("M",   (byte) 0b001);
        asmToHackDest.put("A",   (byte) 0b100);
        asmToHackDest.put("D",   (byte) 0b010);
        //Jump Map
        asmToHackJump.put("null",(byte) 0b000);
        asmToHackJump.put("JGT", (byte) 0b001);
        asmToHackJump.put("JEQ", (byte) 0b010);
        asmToHackJump.put("JGE", (byte) 0b011);
        asmToHackJump.put("JLT", (byte) 0b100);
        asmToHackJump.put("JNE", (byte) 0b101);
        asmToHackJump.put("JLE", (byte) 0b110);
        asmToHackJump.put("JMP", (byte) 0b111);
        //Computation Map
        asmToHackComp.put("0","0101010");
        asmToHackComp.put("1","0111111");
        asmToHackComp.put("-1","0111010");
        asmToHackComp.put("D","0001100");
        asmToHackComp.put("A","0110000");
        asmToHackComp.put("M","1110000");
        asmToHackComp.put("D!","0001101");
        asmToHackComp.put("A!","0110001");
        asmToHackComp.put("M!","1110001");
        asmToHackComp.put("-D","0001111");
        asmToHackComp.put("-A","0110011");
        asmToHackComp.put("-M","1110011");
        asmToHackComp.put("D+1","0011111");
        asmToHackComp.put("1+D",asmToHackComp.get("D+1"));
        asmToHackComp.put("A+1","0110111");
        asmToHackComp.put("1+A",asmToHackComp.get("A+1"));
        asmToHackComp.put("M+1","1110111");
        asmToHackComp.put("1+M",asmToHackComp.get("M+1"));
        asmToHackComp.put("D-1","0001110");
        asmToHackComp.put("A-1","0110010");
        asmToHackComp.put("M-1","1110010");
        asmToHackComp.put("D+A","0000010");
        asmToHackComp.put("A+D",asmToHackComp.get("D+A"));
        asmToHackComp.put("D+M","1000010");
        asmToHackComp.put("M+D",asmToHackComp.get("D+M"));
        asmToHackComp.put("D-A","0010011");
        asmToHackComp.put("D-M","1010011");
        asmToHackComp.put("A-D","0000111");
        asmToHackComp.put("M-D","1000111");
        asmToHackComp.put("D&A","0000000");
        asmToHackComp.put("A&D",asmToHackComp.get("D&A"));
        asmToHackComp.put("D&M","1000000");
        asmToHackComp.put("M&D",asmToHackComp.get("D&M"));
        asmToHackComp.put("D|A","0010101");
        asmToHackComp.put("A|D",asmToHackComp.get("D|A"));
        asmToHackComp.put("D|M","1010101");
        asmToHackComp.put("M|D",asmToHackComp.get("D|M"));
    }
    public static String dest(String destinationRegisters){
        if(destinationRegisters.equals("null")){
            return String.format("%03d",asmToHackDest.get("null"));
        } else {
            byte result = 0;
            for(int i = 0; i < destinationRegisters.length(); i++){
                if(asmToHackDest.containsKey(destinationRegisters.charAt(i) + "")){
                    result = (byte) (result |  asmToHackDest.get(destinationRegisters.charAt(i) + ""));
                }else {
                    throw new RuntimeException();
                }
            }
            String resultStr = Integer.toBinaryString(result);
            while (resultStr.length() < 3){
                resultStr = "0" + resultStr;
            }
            return resultStr;
        }
    }
    public static String comp(String computationPart){
        if(asmToHackComp.containsKey(computationPart)) {
            return asmToHackComp.get(computationPart);
        }else {
            throw new RuntimeException();
        }
    }
    public static String jump(String jumpCondition){
        if(asmToHackJump.containsKey(jumpCondition)){
            String res = Integer.toBinaryString(asmToHackJump.get(jumpCondition));
            while(res.length() < 3){
                res = "0" + res;
            }
            return res;
        }else{
            throw new RuntimeException();
        }
    }

}
