package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * Binary expressions.
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class AbstractBinaryExpr extends AbstractExpr {

    public AbstractExpr getLeftOperand() {
        return leftOperand;
    }

    public AbstractExpr getRightOperand() {
        return rightOperand;
    }

    protected void setLeftOperand(AbstractExpr leftOperand) {
        Validate.notNull(leftOperand);
        this.leftOperand = leftOperand;
    }

    protected void setRightOperand(AbstractExpr rightOperand) {
        Validate.notNull(rightOperand);
        this.rightOperand = rightOperand;
    }

    protected AbstractExpr leftOperand;
    protected AbstractExpr rightOperand;

    public AbstractBinaryExpr(AbstractExpr leftOperand,
            AbstractExpr rightOperand) {
        Validate.notNull(leftOperand, "left operand cannot be null");
        Validate.notNull(rightOperand, "right operand cannot be null");
        Validate.isTrue(leftOperand != rightOperand, "Sharing subtrees is forbidden");
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister register) {
        // we still need a register for any binary op
        GPRegister leftRegister = register == null ? compiler.allocateRegister() : register;
        // call the binary expression code on the given register and the address
        // load left operand in the result register
        leftOperand.codeGenExpr(compiler, leftRegister);
        // load the right operand
        GPRegister rightRegister = compiler.allocateRegister();
        rightOperand.codeGenExpr(compiler, rightRegister);
        // do the operation
        codeGenBinExp(compiler, leftRegister, rightRegister);
        // free right register
        compiler.freeRegister(rightRegister);
        // if the original register is null, load the result on the stack (also need to
        // free the register)
        if (register == null) {
            // load the rsesult in R1 to free the register (free might pop the stack)
            compiler.addInstruction(new LOAD(leftRegister, Register.R1));
            compiler.freeRegister(leftRegister);
            compiler.incrementContextUsedStack();
            compiler.addInstruction(new PUSH(leftRegister));
        }
    }

    /**
     * do the binary operation between the given register and the given DVal.
     * 
     * @param compiler Where we write the instructions.
     * @param register Not null : contains one of the operands, and where we put the
     *                 result.
     */
    public abstract void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dVal);

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        getLeftOperand().decompile(s);
        s.print(" " + getOperatorName() + " ");
        getRightOperand().decompile(s);
        s.print(")");
    }

    abstract protected String getOperatorName();

    @Override
    protected void iterChildren(TreeFunction f) {
        leftOperand.iter(f);
        rightOperand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        leftOperand.prettyPrint(s, prefix, false);
        rightOperand.prettyPrint(s, prefix, true);
    }

    @Override
    public AbstractInst factorise(DecacCompiler compiler){
        leftOperand = (AbstractExpr)leftOperand.factorise(compiler);
        rightOperand = (AbstractExpr)rightOperand.factorise(compiler);
        return this;
    }

    @Override
    public boolean isSplitable(DecacCompiler compiler) {
        return leftOperand.isSplitable(compiler) || rightOperand.isSplitable(compiler);
    }

    @Override
    public AbstractInst splitCalculus(DecacCompiler compiler) {
        if (leftOperand.isSplitable(compiler))
            leftOperand = ((AbstractExpr) leftOperand.splitCalculus(compiler));
        if (rightOperand.isSplitable(compiler))
            rightOperand = ((AbstractExpr) rightOperand.splitCalculus(compiler));
        return this;
    }

    @Override
    protected void spotUsedVar() {
        this.leftOperand.spotUsedVar();
        this.rightOperand.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar() {
        this.leftOperand = (AbstractExpr)this.leftOperand.removeUnusedVar();
        this.rightOperand = (AbstractExpr)this.rightOperand.removeUnusedVar();
        return this;
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        this.leftOperand.addUnremovableExpr(foundMethodCalls);
        this.rightOperand.addUnremovableExpr(foundMethodCalls);
    }

    @Override
    public CollapseResult<CollapseValue> collapseExpr() {
        return collapseBinExpr();
    }

    public abstract CollapseResult<CollapseValue> collapseBinExpr();
    
    
    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.leftOperand = (AbstractExpr)this.leftOperand.doSubstituteInlineMethods(inlineMethods);
        this.rightOperand = (AbstractExpr)this.rightOperand.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    @Override
    protected boolean containsField() {
        return this.leftOperand.containsField() || this.rightOperand.containsField();
    }

    @Override
    public boolean irrelevant() {
        if (inWhile) return false;
        if (inField){
            HashMap<Symbol, AbstractExpr> actualDico = varModels.get(actualClass);
            boolean irrelevantRight = false;
            if (getRightOperand().isSelection()){
                AbstractExpr out = ((Selection) getRightOperand()).returnIrrelevantFromSelection();
                if (out != null) {
                    setRightOperand(out);
                }
                if (getRightOperand().isSelection()) irrelevantRight = ((Selection) getRightOperand()).isKnown();
            }
            else if (getRightOperand().irrelevant() && actualDico.containsKey(((Identifier) getRightOperand()).getName())) {
                rightOperand = actualDico.get(((Identifier) getRightOperand()).getName());
                irrelevantRight = (getRightOperand().irrelevant() && actualDico.containsKey(((Identifier) getRightOperand()).getName()));
            }

            boolean irrelevantLeft = false;
            if (getLeftOperand().isSelection()){
                AbstractExpr out = ((Selection) getLeftOperand()).returnIrrelevantFromSelection();
                if (out != null) {
                    setLeftOperand(out);
                }
                if (getLeftOperand().isSelection()) irrelevantLeft = ((Selection) getLeftOperand()).isKnown();
            }
            else if (getLeftOperand().irrelevant() && actualDico.containsKey(((Identifier) getLeftOperand()).getName())) {
                leftOperand = actualDico.get(((Identifier) getLeftOperand()).getName());
                irrelevantLeft = (getLeftOperand().irrelevant() && actualDico.containsKey(((Identifier) getLeftOperand()).getName()));
            }

            return irrelevantLeft || irrelevantRight || (!getLeftOperand().isSelection() && 
            ((leftOperand.irrelevant() && actualDico.containsKey(((Identifier) getLeftOperand()).getName())) || (rightOperand.irrelevant() && actualDico.containsKey(((Identifier) getRightOperand()).getName()))));

        } else {

            boolean irrelevantRight = false;
            if (getRightOperand().isSelection()){
                AbstractExpr out = ((Selection) getRightOperand()).returnIrrelevantFromSelection();
                if (out != null) {
                    setRightOperand(out);
                }
                if (getRightOperand().isSelection()) irrelevantRight = ((Selection) getRightOperand()).isKnown();
            }
            else if (getRightOperand().irrelevant() && currentValues.containsKey(((Identifier) getRightOperand()).getName())) {
                rightOperand = currentValues.get(((Identifier) getRightOperand()).getName());
                irrelevantRight = (getRightOperand().irrelevant() && currentValues.containsKey(((Identifier) getRightOperand()).getName()));
            }

            boolean irrelevantLeft = false;
            if (getLeftOperand().isSelection()){
                AbstractExpr out = ((Selection) getLeftOperand()).returnIrrelevantFromSelection();
                if (out != null) {
                    setLeftOperand(out);
                }
                if (getLeftOperand().isSelection()) irrelevantLeft = ((Selection) getLeftOperand()).isKnown();
            }
            else if (getLeftOperand().irrelevant() && currentValues.containsKey(((Identifier) getLeftOperand()).getName())) {
                leftOperand = currentValues.get(((Identifier) getLeftOperand()).getName());
                irrelevantLeft = (getLeftOperand().irrelevant() && currentValues.containsKey(((Identifier) getLeftOperand()).getName()));
            }

            return irrelevantLeft || irrelevantRight || (!getLeftOperand().isSelection() && 
            ((leftOperand.irrelevant() && currentValues.containsKey(((Identifier) getLeftOperand()).getName())) || (rightOperand.irrelevant() && currentValues.containsKey(((Identifier) getRightOperand()).getName()))));
        }
    } 

    @Override
    public boolean irrelevant(int i) {
        boolean irrelevantRight = false;
        if (getRightOperand().isSelection()){
            AbstractExpr out = ((Selection) getRightOperand()).returnIrrelevantFromSelection(i);
            if (out != null) {
                setRightOperand(out);
            }
            if (getRightOperand().isSelection()) irrelevantRight = ((Selection) getRightOperand()).isKnown(i);
        }
        else if (getRightOperand().irrelevant(i) && irrelevantValuesForIf.get(i).containsKey(((Identifier) getRightOperand()).getName())) {
            rightOperand = irrelevantValuesForIf.get(i).get(((Identifier) getRightOperand()).getName());
            irrelevantRight = (getRightOperand().irrelevant(i) && irrelevantValuesForIf.get(i).containsKey(((Identifier) getRightOperand()).getName()));
        }

        boolean irrelevantLeft = false;
        if (getLeftOperand().isSelection()){
            AbstractExpr out = ((Selection) getLeftOperand()).returnIrrelevantFromSelection(i);
            if (out != null) {
                setLeftOperand(out);
            }
            if (getLeftOperand().isSelection()) irrelevantLeft = ((Selection) getLeftOperand()).isKnown(i);
        }
        else if (getLeftOperand().irrelevant(i) && irrelevantValuesForIf.get(i).containsKey(((Identifier) getLeftOperand()).getName())) {
            leftOperand = irrelevantValuesForIf.get(i).get(((Identifier) getLeftOperand()).getName());
            irrelevantLeft = (getLeftOperand().irrelevant(i) && irrelevantValuesForIf.get(i).containsKey(((Identifier) getLeftOperand()).getName()));
        }

        return irrelevantLeft || irrelevantRight || (!getLeftOperand().isSelection() && 
        ((leftOperand.irrelevant(i) && irrelevantValuesForIf.get(i).containsKey(((Identifier) getLeftOperand()).getName())) || (rightOperand.irrelevant(i) && irrelevantValuesForIf.get(i).containsKey(((Identifier) getRightOperand()).getName()))));
        
    } 

    @Override
    public boolean isReadExpr() {
        return leftOperand.isReadExpr() || rightOperand.isReadExpr();
    }
}
