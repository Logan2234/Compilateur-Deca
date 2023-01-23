package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.context.EnvironmentExp.DoubleDefException;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import fr.ensimag.ima.pseudocode.RegisterOffset;

/**
 * Declaration of a variable
 * 
 * @author gl03
 * @date 01/01/2023
 */
public class DeclVar extends AbstractDeclVar {

    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        Type type = this.type.verifyType(compiler);

        if (type.isVoid())
            throw new ContextualError("The variable's type can't be void (rule 3.17)", getLocation());

        try {
            ExpDefinition def = new VariableDefinition(type, getLocation());
            initialization.verifyInitialization(compiler, type, localEnv, currentClass);
            localEnv.declare(varName.getName(), def);
            varName.verifyExpr(compiler, localEnv, currentClass);
        } catch (DoubleDefException e) {
            throw new ContextualError(
                    "The variable \"" + varName.getName().getName() + "\" has already been declared (rule 3.17)",
                    getLocation());
        }

    }

    @Override
    public void codeGenDeclVar(DecacCompiler compiler, RegisterOffset register) {
        // store the register in the definition of the variable, then assign it with the
        // initialization.
        varName.getDefinition().setDAddr(register);
        // store the variable at the address now, using a push as we are declaring all
        // variables.
        initialization.codeGenInit(compiler, type.getType(), register);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(' ');
        varName.decompile(s);
        initialization.decompile(s);
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    @Override
    protected void spotUsedVar() {
        // We don't spotUsedVar() on the type (it may be a class) or the identifier as they are just declared.
        this.initialization.spotUsedVar();
    }

    @Override
    protected Tree removeUnusedVar(Program prog) {
        this.initialization = (AbstractInitialization)this.initialization.removeUnusedVar(prog);
        if (this.varName.getDefinition().isUsed()) {
            return this;
        }
        if (this.initialization.getExpression() == null){
            prog.setVarRemoved();
            return null;
        }
        List<AbstractExpr> listExpr = this.initialization.getExpression().getUnremovableExpr();
        if (listExpr.isEmpty()) {
            prog.setVarRemoved();
            return null;
        }
        if (this.type.getDefinition().getType().isInt() || this.type.getDefinition().getType().isFloat()) {
            Iterator<AbstractExpr> iter = listExpr.iterator();
            AbstractExpr expr = iter.next();
            Location loc = expr.getLocation();
            while (iter.hasNext()) {
                loc = expr.getLocation();
                expr = new Plus(expr, iter.next());
                expr.setLocation(loc);
            }
            this.initialization = new Initialization(expr);
            this.initialization.setLocation(loc);
        }
        return this;
    }

    public AbstractIdentifier getVar() {
        return this.varName;
    }

    public AbstractInitialization getInit() {
        return this.initialization;
    }

    @Override
    public CollapseResult<Null> collapseDeclVar() {
        CollapseResult<CollapseValue> result = initialization.collapseInit();
        // look in the collapse value if we can change the init node
        if(type.getType().isBoolean() && result.getResult().isBool()) {
            initialization = new Initialization(new BooleanLiteral(result.getResult().asBool()));
        }
        else if(type.getType().isFloat() && result.getResult().isFloat()) {
            initialization = new Initialization(new FloatLiteral(result.getResult().asFloat()));
        }
        if(type.getType().isInt() && result.getResult().isInt()) {
            initialization = new Initialization(new IntLiteral(result.getResult().asInt()));
        }
        return new CollapseResult<Null>(null, result.couldCollapse());
    }

    @Override
    protected Tree doSubstituteInlineMethods(Map<MethodDefinition, DeclMethod> inlineMethods) {
        this.initialization = (AbstractInitialization)this.initialization.doSubstituteInlineMethods(inlineMethods);
        return this;
    }


    @Override
    public AbstractInst factorise(DecacCompiler compiler) {
        initialization.factorise(compiler);
        return null;
    }

    @Override
    public boolean isSplitable(DecacCompiler compiler) {
        return initialization.isSplitable(compiler);
    }

    @Override
    public AbstractInst splitCalculus(DecacCompiler compiler){
        initialization.splitCalculus(compiler);
        return null;
    }
}
