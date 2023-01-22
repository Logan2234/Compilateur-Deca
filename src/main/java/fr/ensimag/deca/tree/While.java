package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import javax.swing.text.html.HTMLDocument.Iterator;

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
        GPRegister condRegister = compiler.allocateRegister();
        condition.codeGenExpr(compiler, condRegister);
        compiler.addInstruction(new CMP(new ImmediateInteger(0), condRegister));
        compiler.freeRegister(condRegister);
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
    protected void spotUsedVar() {
        this.condition.spotUsedVar();
        this.body.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar() {
        this.condition = (AbstractExpr) this.condition.removeUnusedVar();
        this.body = (ListInst) this.body.removeUnusedVar();
        if (!this.body.isEmpty()) {
            return this;
        }
        List<AbstractExpr> unremovableExpressions = this.condition.getUnremovableExpr();
        if (unremovableExpressions.isEmpty()) {
            return null;
        } 
        return this;
    }

    @Override
    public boolean collapse() {
        return condition.collapse() || body.collapse();
    }

    @Override
    public ListInst collapseInst() {
        Boolean collapsedCond = condition.collapseBool();
        if(collapsedCond != null) {
            // if it's true, get out of block the body
            if(collapsedCond) {
                ListInst result = new ListInst();
                for(AbstractInst i : body.getList()) {
                    // ! dangerous ...
                    // result.add(i);
                }
                result.add(this);
                return result;
            }
            // if not, skip while
            else {
                return new ListInst();
            }
        }
        ListInst result = new ListInst();
        result.add(this);
        return result;
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.condition = (AbstractExpr)this.condition.doSubstituteInlineMethods(inlineMethods);
        this. body = (ListInst)this.body.doSubstituteInlineMethods(inlineMethods);
        return this;
    }
}
