package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.ima.pseudocode.instructions.SNE;


import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * InstanceOf Statment
 *
 * @author Jorge Luri Vañó
 * @date 09/01/2023
 */
public class InstanceOf extends AbstractExpr {

    private AbstractExpr expression;
    private final AbstractIdentifier type;

    public InstanceOf(AbstractExpr expression, AbstractIdentifier type) {
        Validate.notNull(expression);
        Validate.notNull(type);
        this.expression = expression;
        this.type = type;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type typeE = expression.verifyExpr(compiler, localEnv, currentClass);
        Type typeT = type.verifyType(compiler);
        if (!typeE.isClassOrNull() || !typeT.isClass()) {
            throw new ContextualError("instanceof argument has to be a class (rule 3.40)", getLocation());
        }
        
        // Ajout du décor
        setType(compiler.environmentType.BOOLEAN);
        return compiler.environmentType.BOOLEAN;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        expression.decompile(s);
        s.print(" instanceof ");
        type.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expression.iter(f);
        type.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, false);
        type.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // compute the result of the expression (pointer to object) in a register.
        GPRegister exprRegister = compiler.allocateRegister();
        expression.codeGenExpr(compiler, exprRegister);
        compiler.addInstruction(new LOAD(exprRegister, Register.R1));
        compiler.freeRegister(exprRegister);
        // get a register to put the comparing value in 
        GPRegister register = resultRegister == null ? compiler.allocateRegister() : resultRegister;
        // put the comparing value in it
        compiler.addInstruction(new LEA(type.getDefinition().getDAddr(), register));
        // load the pointer to the class of the object in that same register
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));
        // if R1 is null, return false. if R1 is the type, return true. else, loop
        compiler.addLabel(new Label("instanceof.loop." + getLocation().toLabel()));
        compiler.addInstruction(new CMP(new NullOperand(), Register.R1));
        compiler.addInstruction(new SNE(Register.R0)); // if R1 is null, than a sne will load a zero in R0 for result.
        compiler.addInstruction(new BEQ(new Label("instanceof.end." + getLocation().toLabel())));
        compiler.addInstruction(new CMP(register, Register.R1)); // test against the obj
        compiler.addInstruction(new SEQ(Register.R0)); // if equals, put 1 in R0 as the result ! 
        compiler.addInstruction(new BEQ(new Label("instanceof.end." + getLocation().toLabel())));
        // still not branching out ? keep looping !
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R1), Register.R1));
        compiler.addInstruction(new BRA(new Label("instanceof.loop." + getLocation().toLabel())));
        compiler.addLabel(new Label("instanceof.end." + getLocation().toLabel()));

        if(resultRegister == null) {
            // free the registered allocated
            compiler.freeRegister(register);
            // push result on the stack
            compiler.increaseContextUsedStack(1);
            compiler.addInstruction(new PUSH(Register.R0));
        }
        else {
            // load the result in the result register
            compiler.addInstruction(new LOAD(Register.R0, resultRegister));
        }
    }

    @Override
    protected void spotUsedVar() {
        // We don't spotUsedVar on the class type by default
        // If the class is not used elsewhere then the expression will be evaluated to false.
        if (!this.expression.getUnremovableExpr().isEmpty()) {
            // we cannot remove the instanceof so we have to spot the type
            this.type.spotUsedVar();
        }
        this.expression.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar(Program prog) {
        if (!this.type.getClassDefinition().isUsed()) {
            BooleanLiteral bool = new BooleanLiteral(false);
            bool.setLocation(this.getLocation());
            return bool; 
        }
        this.expression = (AbstractExpr)this.expression.removeUnusedVar(prog);
        return this;
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        // the expression could be obtained via a MethodCall
        this.expression.addUnremovableExpr(foundMethodCalls);
    }

    public AbstractExpr getExpr() {
        return this.expression;
    }

    @Override
    public CollapseResult<CollapseValue> collapseExpr() {
        // nothing to collapse here
        return new CollapseResult<CollapseValue>(new CollapseValue(), false);
    }
    
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.expression = (AbstractExpr)this.expression.doSubstituteInlineMethods(inlineMethods);
        return this;
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        AbstractExpr res = new InstanceOf(this.expression.substitute(substitutionTable),(AbstractIdentifier) this.type.substitute(substitutionTable));
        res.setType(this.getType());
        res.setLocation(this.getLocation());
        return res;
    }

    @Override
    protected boolean containsField() {
        return this.expression.containsField() || this.type.containsField();
    }
}