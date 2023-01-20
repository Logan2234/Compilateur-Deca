package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;

/**
 * @author gl03
 * @date 01/01/2023
 */
public class Print extends AbstractPrint {
    /**
     * @param arguments arguments passed to the print(...) statement.
     * @param printHex if true, then float should be displayed as hexadecimal (printx)
     */
    public Print(boolean printHex, ListExpr arguments) {
        super(printHex, arguments);
    }

    @Override
    String getSuffix() {
        return "";
    }

    public boolean factorised(DecacCompiler compiler) {
        return false;//TODO
    }
    public boolean collapse() {
        return false;
    }

    @Override
    public ListInst collapseInst() {
        ListInst result = new ListInst();
        result.add(this);
        return result;
    }
    @Override
    public ListInst factoInst(DecacCompiler compiler) {
        ListInst result = new ListInst();
        result.add(this);
        return result;
    }
}
