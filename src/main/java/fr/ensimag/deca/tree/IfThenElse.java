package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;

import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Full if/else if/else statement.
 *
 * @author gl03
 * @date 01/01/2023
 */
public class IfThenElse extends AbstractInst {

    private final AbstractExpr condition;
    private final ListInst thenBranch;
    private ListInst elseBranch;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {
        this.condition.verifyCondition(compiler, localEnv, currentClass);
        this.thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
        this.elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // we need something like : 
        // if not condition, jump to else block
        // if block
        //      [...]
        //      jump to end
        // else block
        //      [...]
        // end
        
        // need a unique label for our name.
        String label = "IF." + getLocation().toLabel();
        Label elseLabel = new Label(label + ".else");
        Label endLabel = new Label(label + ".end");
        // the if expression returns a bool. write it down in R1,
        // then add 0 to R1 to trigger flags : if EQ, then the expression was false : branch to else block
        condition.codeGenExpr(compiler, Register.R1);
        compiler.addInstruction(new ADD(new ImmediateInteger(0), Register.R1));
        // branch to else flag if EQ, then if block
        compiler.addInstruction(new BEQ(elseLabel));
        thenBranch.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(endLabel));
        // else flag to branch to, and else block
        compiler.addLabel(elseLabel);
        elseBranch.codeGenListInst(compiler);
        // end label
        compiler.addLabel(endLabel);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("if (");
        condition.decompile(s);
        s.println("){");
        s.indent();
        thenBranch.decompile(s);
        s.unindent();
        s.println("} else {");
        s.indent();
        elseBranch.decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }

    @Override
    public boolean collapse() {
        return condition.collapse() || thenBranch.collapse() || elseBranch.collapse();
    }

    @Override
    public ListInst collapseInst() {
        // try to collapse the condition
        Boolean collapsedCond = condition.collapseBool();
        if(collapsedCond != null) {
            // we can collapse whole if block !
            if(collapsedCond) {
                return thenBranch;
            }
            else {
                return elseBranch;
            }
        }
        // I mean, sadly return ourself :(
        ListInst result = new ListInst();
        result.add(this);
        return result;
    }



}
