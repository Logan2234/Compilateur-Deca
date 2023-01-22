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
        AbstractExpr expr;
        for (int i = 0; i < getList().size(); i++) {
            expr = getList().get(i);
            if (expr.irrelevant() || expr.isSelection()){
                if (expr.isSelection()){
                    AbstractExpr out = ((Selection) expr).returnIrrelevantFromSelection();
                    if (out != null) {
                        result |= true;
                        set(i, out);
                    } 
                }
                else {
                    if (currentValues.containsKey(((Identifier) expr).getName())){
                        result |= true;
                        set(i, currentValues.get(((Identifier) expr).getName()));
                    }
                }
            }
        }
        return result;
    }
}
