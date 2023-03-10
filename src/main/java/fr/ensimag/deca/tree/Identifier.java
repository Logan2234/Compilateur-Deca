package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

/**
 * Deca Identifier
 *
 * @author gl03
 * @date 01/01/2023
 */
public class Identifier extends AbstractIdentifier {

    @Override
    protected void checkDecoration() {
        if (getDefinition() == null) {
            throw new DecacInternalError("Identifier " + this.getName() + " has no attached Definition");
        }
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ClassDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a class definition.
     */
    @Override
    public ClassDefinition getClassDefinition() {
        try {
            return (ClassDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a class identifier, you can't call getClassDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * MethodDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a method definition.
     */
    @Override
    public MethodDefinition getMethodDefinition() {
        try {
            return (MethodDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a method identifier, you can't call getMethodDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * FieldDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a field definition.
     */
    @Override
    public FieldDefinition getFieldDefinition() {
        try {
            return (FieldDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a field identifier, you can't call getFieldDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * VariableDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a variable definition.
     */
    @Override
    public VariableDefinition getVariableDefinition() {
        try {
            return (VariableDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a variable identifier, you can't call getVariableDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ParamDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not a parameter definition.
     */
    @Override
    public ParamDefinition getParamDefinition() {
        try {
            return (ParamDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a parameter identifier, you can't call getParamDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ExpDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *                            if the definition is not an expression definition.
     */
    @Override
    public ExpDefinition getExpDefinition() {
        try {
            return (ExpDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a Exp identifier, you can't call getExpDefinition on it");
        }
    }

    @Override
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public Symbol getName() {
        return name;
    }

    private Symbol name;

    public Identifier(Symbol name) {
        Validate.notNull(name);
        this.name = name;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Definition def = localEnv.get(name);

        if (def == null)
            throw new ContextualError("The identifier \"" + name.getName() + "\" doesn't exist (rule 0.1)",
                    getLocation());

        // Ajout du d??cor
        Type type = def.getType();
        setDefinition(def);
        setType(type);
        return type;
    }

    /**
     * Implements non-terminal "type" of [SyntaxeContextuelle] in the 3 passes
     * 
     * @param compiler contains "env_types" attribute
     */
    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        TypeDefinition def = compiler.environmentType.defOfType(name);

        if (def == null)
            throw new ContextualError("The type \"" + name + "\" doesn't exist (rule 0.2)", getLocation());

        // Ajout du d??cor
        Type type = def.getType();
        setDefinition(def);
        setType(type);
        return type;
    }

    private Definition definition;

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(name.toString());
    }

    @Override
    String prettyPrintNode() {
        return "Identifier (" + getName() + ")";
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Definition d = getDefinition();
        if (d != null) {
            s.print(prefix);
            s.print("definition: ");
            s.print(d);
            s.println();
        }
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler, boolean hex) {
        // if it is a field, we need to first load the value on from the heap !
        if(getDefinition().isField()) {
            GPRegister classPointerRegister = compiler.allocateRegister();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), classPointerRegister));
            compiler.addInstruction(new LOAD(new RegisterOffset(definition.getDAddrOffsetOnly(), classPointerRegister), Register.R1));
            compiler.freeRegister(classPointerRegister);
            if (definition.getType().isInt()) {
                // print it
                compiler.addInstruction(new WINT());
            } else if (definition.getType().isFloat()) {
                // print it
                if (hex) {
                    compiler.addInstruction(new WFLOATX());
                } else {
                    compiler.addInstruction(new WFLOAT());
                }
            }
        }
        else {
            if (definition.getType().isInt()) {
                // print identifier as an int :
                // load addr in R1
                compiler.addInstruction(new LOAD(definition.getDAddr(), Register.R1));
                // print it
    
                compiler.addInstruction(new WINT());
            } else if (definition.getType().isFloat()) {
                // print identifier as an float :
                // load addr in R1
                compiler.addInstruction(new LOAD(definition.getDAddr(), Register.R1));
                // print it
                if (hex) {
                    compiler.addInstruction(new WFLOATX());
                } else {
                    compiler.addInstruction(new WFLOAT());
                }
            }
        }

    }

    /**
     * Generate the code that put the value in the register.
     */
    @Override
    protected void codeGenExpr(DecacCompiler compiler, GPRegister resultRegister) {
        // if it is a field, we need to first load the value on from the heap !
        if (getDefinition().isField()) {
            GPRegister classPointerRegister = compiler.allocateRegister();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), classPointerRegister));
            compiler.addInstruction(new LOAD(new RegisterOffset(definition.getDAddrOffsetOnly(), classPointerRegister),
                    classPointerRegister));
            if (resultRegister == null) {
                // put self value on the stack
                // load self on R1, then push R1
                compiler.addInstruction(new LOAD(classPointerRegister, Register.R1));
                compiler.freeRegister(classPointerRegister);
                compiler.incrementContextUsedStack();
                compiler.addInstruction(new PUSH(Register.R1));
            } else {
                // put self value in the result register
                compiler.addInstruction(new LOAD(classPointerRegister, resultRegister));
                compiler.freeRegister(classPointerRegister);
            }
        } else {
            if (resultRegister == null) {
                // put self value on the stack
                // load self on R1, then push R1
                compiler.addInstruction(new LOAD(definition.getDAddr(), Register.R1));
                compiler.incrementContextUsedStack();
                compiler.addInstruction(new PUSH(Register.R1));
            } else {
                // put self value in the result register
                compiler.addInstruction(new LOAD(definition.getDAddr(), resultRegister));
            }
        }

    }

    @Override
    public void codeGenAssignLVal(DecacCompiler compiler, GPRegister register) {
        // if it is a field, we need to first load the value on from the heap !
        if(getDefinition().isField()) {
            GPRegister classPointerRegister = compiler.allocateRegister();
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), classPointerRegister));
            if (register == null) {
                // get value to assign from the stack
                compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), Register.R1));
                compiler.freeRegister(classPointerRegister);
                compiler.addInstruction(new STORE(Register.R1, new RegisterOffset(definition.getDAddrOffsetOnly(), classPointerRegister)));
            } else {
                // get value to assign from registe
                compiler.addInstruction(new STORE(register, new RegisterOffset(definition.getDAddrOffsetOnly(), classPointerRegister)));
                compiler.freeRegister(classPointerRegister);
            }
        }
        else {
            if (register == null) {
                // get value to assign from the stack
                compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), Register.R1));
                compiler.addInstruction(new STORE(Register.R1, definition.getDAddr()));
            } else {
                // get value to assign from registe
                compiler.addInstruction(new STORE(register, definition.getDAddr()));
            }
        }
    }



    @Override
    public CollapseResult<CollapseValue> collapseExpr() {
        // nothing to collapse on identifier !
        return new CollapseResult<CollapseValue>(new CollapseValue(), false);
    }

    @Override
    protected void spotUsedVar() {
        this.definition.spotUsedVar();
    }

    @Override
    protected void spotOverridingFields(Map<Symbol,Set<ClassDefinition>> usedFields) {
        assert(this.containsField());
        this.getFieldDefinition().spotOverridingFields(this.name,usedFields);

    }

    @Override
    protected Tree removeUnusedVar(Program prog) {
        return this;
    }

    @Override
    protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
        // do nothing
    }

    @Override
    protected Boolean isIdentifier() {
        return true;
    }

    @Override
    protected AbstractExpr substitute(Map<ParamDefinition,AbstractExpr> substitutionTable) {
        if (!substitutionTable.containsKey(this.definition)) {
            return this;
        }
        return substitutionTable.get(this.definition);
    }

    @Override
    protected boolean containsField() {
        return this.getDefinition().isField();
    }

    @Override
    protected boolean isAtomic() {
        return true;
    }
    
}
