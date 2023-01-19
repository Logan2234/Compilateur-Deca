package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl03
 * @date 01/01/2023
 */
public class ListExpr extends TreeList<AbstractExpr> {


    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractExpr i : getList()) {
            if (!(getList().get(0).equals(i))) // ? Not sure if we have param1,param2,param3 ... at the end
                s.print(", ");

            i.decompile(s);
        }
    }

    @Override
    public boolean collapse() {
        // try to collapse each decl var
        boolean collapsed = false;
        for(AbstractExpr i : getList()) {
            collapsed |= i.collapse();
        }
        return collapsed;
    }

    @Override
    public boolean irrelevant() {
        boolean result = false;
        for (int i = 0; i < getList().size(); i++) {
            if (getList().get(i).irrelevant()){
                result |= true;
                if (getList().get(i).getType().isFloat()) {
                    Float rightIrrelevantdValue = getList().get(i).irrelevantFloat();
                    if(rightIrrelevantdValue != null && getList().get(i).irrelevantable()) {
                        FloatLiteral newFloat = new FloatLiteral(rightIrrelevantdValue);
                        newFloat.setType(getList().get(i).getType());
                        set(i, newFloat);
                    }
                }
    
                if (getList().get(i).getType().isInt()){
                    Integer rightIrrelevantdValue = getList().get(i).irrelevantInt();
                    if(rightIrrelevantdValue != null && getList().get(i).irrelevantable()) {
                        IntLiteral newInt = new IntLiteral(rightIrrelevantdValue);
                        newInt.setType(getList().get(i).getType());
                        set(i, newInt);
                    }
                }
    
                if (getList().get(i).getType().isBoolean()){
                    Boolean rightIrrelevantdValue = getList().get(i).irrelevantBool();
                    if(rightIrrelevantdValue != null && getList().get(i).irrelevantable()) {
                        BooleanLiteral newBool = new BooleanLiteral(rightIrrelevantdValue);
                        newBool.setType(getList().get(i).getType());
                        set(i, newBool);
                    }
                }
            }
        }
        return result;
    }
}
