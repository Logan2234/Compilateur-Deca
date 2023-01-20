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
 *
 * @author gl03
 * @date 01/01/2023
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;

    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        // what to do :
        // if not condition, jump to end label
        // [...] code
        // jump to block code
        // end label

        // need a unique label for our name.
        String label = "WHILE." + getLocation().toLabel();
        Label blockLabel = new Label(label + ".while");
        Label endLabel = new Label(label + ".end");
        // the if expression returns a bool. write it down in R1,
        // then add 0 to R1 to trigger flags : if EQ, then the expression was false :
        // branch to end block
        compiler.addLabel(blockLabel);
        condition.codeGenExpr(compiler, Register.R1);
        compiler.addInstruction(new ADD(new ImmediateInteger(0), Register.R1));
        // branch to else flag if EQ, then if block
        compiler.addInstruction(new BEQ(endLabel));
        // main block
        body.codeGenListInst(compiler);
        // jump to start
        compiler.addInstruction(new BRA(blockLabel));
        compiler.addLabel(endLabel);
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass,
            Type returnType) throws ContextualError {
        condition.verifyCondition(compiler, localEnv, currentClass);
        body.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

    @Override
    protected void spotUsedVar(AbstractProgram prog) {
        this.condition.spotUsedVar(prog);
        this.body.spotUsedVar(prog);
    }
}
