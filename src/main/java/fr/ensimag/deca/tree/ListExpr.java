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
        for (AbstractExpr i : getList()) {
            result |= i.irrelevant();
        }
        return result;
    }
}
