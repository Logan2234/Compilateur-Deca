package fr.ensimag.ima.pseudocode;

import java.io.PrintStream;

public class UserAsmBlock extends Line {
    
    private final String[] code;

    public UserAsmBlock(String code) {
        super(null, null, null);
        String[] tempCode = code.split("\n");
        for(int i = 0; i < tempCode.length; i++) {
            tempCode[i].replace("\t", "");
            tempCode[i].replaceFirst("^\\s*", "");
        }
        this.code = tempCode;
    }

    @Override
    public void display(PrintStream s) {
        for(String codeLine : code) {
            s.print("\t");
            s.println(codeLine);
        }
    }
}
