package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.Register;

/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class ListDeclVar extends TreeList<AbstractDeclVar> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclVar i : getList()) {
            i.decompile(s);
            s.println();
        }
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * This represents the size of the stack of declared variables.
     * It get increased when declaring a new variable, and passed so that variable knows is address.
     * The variables are stored at LB + var offset.
     * It starts at one because it is said that at the start, LB = GB, and GB is before the first stack obect.
     * That means that with one variable, it is at GB + 1 so the first variable will be pointing at LB+1.
     */
    private int varStackSize = 1;

    /**
     * Code generatio to declare all variables. 
     * This also keep track of the number of variables, and assign to each of them a DAddr to keep track of where they are.
     * @param compiler Where we write our instructions to.
     */
    public void codeGenDeclVar(DecacCompiler compiler) {
        for(AbstractDeclVar i : getList()) {
            // create the DAddr that references the variable
            RegisterOffset register = new RegisterOffset(varStackSize, Register.LB);
            i.codeGenDeclVar(compiler, register);
            // increment the number of allocated variables
            varStackSize++;
        }
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains the "env_types" attribute
     * @param localEnv 
     *   its "parentEnvironment" corresponds to "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to 
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to 
     *      the "env_exp_r" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     */    
    void verifyListDeclVariable(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
    }


}
