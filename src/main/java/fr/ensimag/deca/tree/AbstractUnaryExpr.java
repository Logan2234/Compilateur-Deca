package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Unary expression.
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractUnaryExpr extends AbstractExpr {

    public AbstractExpr getOperand() {
        return operand;
    }

    protected AbstractExpr operand;

    public AbstractUnaryExpr(AbstractExpr operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }

    public void setOperand(AbstractExpr operand){
        Validate.notNull(operand);
        this.operand = operand;
    }


    protected abstract String getOperatorName();

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        s.print(getOperatorName());
        getOperand().decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        operand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        operand.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // as for binary exp, put expr in register then apply codeGenUnExpr
        GPRegister register  = resultRegister == null ? compiler.allocateRegister() : resultRegister;
        // generate code for the expression in the register
        getOperand().codeGenExpr(compiler, register);
        // generate our code on the register containing the result
        codeGenUnExpr(compiler, register);
        // if original register was null, we allocated a register and we need to push result on the stack
        if(resultRegister == null) {
            // need to free result register and put the result on the stack
            compiler.addInstruction(new LOAD(register, Register.R1));
            compiler.freeRegister(register); // pops
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(Register.R1));
        }
        else {
            compiler.addInstruction(new LOAD(register, resultRegister));
        }
    }

    /**
     * Generate the code for the unary expression, with the result regsiter being
     * not null,
     * and the expression being already computed and in the register.
     * 
     * @param compiler      Where we write the instructions to
     * @param resulRegister not null. the expression have been computed and is in
     *                      this register.
     */
    public abstract void codeGenUnExpr(DecacCompiler compiler, GPRegister resulRegister);
    
    @Override
    protected void spotUsedVar() {
        this.operand.spotUsedVar();
    }

    @Override
    public boolean irrelevant() {
        if (inField){
            HashMap<Symbol, AbstractExpr> actualDico = varModels.get(actualClass);
            boolean irrelevant = false;
            if (operand.isSelection()){
                AbstractExpr out = ((Selection) operand).returnIrrelevantFromSelection();
                if (out != null) {
                    operand = out;
                }
                if (operand.isSelection()) irrelevant = ((Selection) operand).isKnown();
            }
            else if (operand.irrelevant() && actualDico.containsKey(((Identifier) operand).getName())) {
                operand = actualDico.get(((Identifier) operand).getName());
            }
    
            return irrelevant || (!operand.isSelection() && 
            (operand.irrelevant() && actualDico.containsKey(((Identifier) operand).getName())));
        } else {

            boolean irrelevant = false;
            if (operand.isSelection()){
                AbstractExpr out = ((Selection) operand).returnIrrelevantFromSelection();
                if (out != null) {
                    operand = out;
                }
                if (operand.isSelection()) irrelevant = ((Selection) operand).isKnown();
            }
            else if (operand.irrelevant() && currentValues.containsKey(((Identifier) operand).getName())) {
                operand = currentValues.get(((Identifier) operand).getName());
            }
            
            return irrelevant || (!operand.isSelection() && 
            (operand.irrelevant() && currentValues.containsKey(((Identifier) operand).getName())));
        }
    
    }

    @Override
    public boolean irrelevant(int i) {
        boolean irrelevant = false;
        if (operand.isSelection()){
            AbstractExpr out = ((Selection) operand).returnIrrelevantFromSelection(i);
            if (out != null) {
                operand = out;
            }
            if (operand.isSelection()) irrelevant = ((Selection) operand).isKnown(i);
        }
        else if (operand.irrelevant(i) && irrelevantValuesForIf.get(i).containsKey(((Identifier) operand).getName())) {
            operand = irrelevantValuesForIf.get(i).get(((Identifier) operand).getName());
        }

        return irrelevant || (!operand.isSelection() && 
        (operand.irrelevant(i) && irrelevantValuesForIf.get(i).containsKey(((Identifier) operand).getName())));
    
    }

    @Override
    public boolean isReadExpr() {
        return operand.isReadExpr();
    }

    protected Tree removeUnusedVar() {
        this.operand = (AbstractExpr)this.operand.removeUnusedVar();
        return this;
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        this.operand.addUnremovableExpr(foundMethodCalls);
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.operand = (AbstractExpr)this.operand.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    @Override
    protected boolean containsField() {
        return this.operand.containsField();
    }

    @Override
    public CollapseResult<CollapseValue> collapseExpr() {
        return collapseUnExpr();
    }

    public abstract CollapseResult<CollapseValue> collapseUnExpr();
    
    
}
