package fr.ensimag.deca.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.codegen.runtimeErrors.OpOverflowErr;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.GPRegister;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.BOV;

/**
 * @author gl03
 * @date 01/01/2023
 */
public class Plus extends AbstractOpArith {
    public Plus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "+";
    }

    @Override
    public void codeGenBinExp(DecacCompiler compiler, GPRegister register, DVal dVal) {
        // add
        compiler.addInstruction(new ADD(dVal, register));
        // check overflow
        if (getType().isFloat() && compiler.getCompilerOptions().getRunTestChecks()) {
            OpOverflowErr error = new OpOverflowErr();
            compiler.useRuntimeError(error);
            compiler.addInstruction(new BOV(error.getErrorLabel()));
        }
    }

    private boolean addId(List idSide, AbstractExpr side){
        boolean bool = false;
        try {
            String idLeft = ((Identifier)side).getName().getName(); // Soit c'est un identifier, on ajoute son nom dans la liste des ID
            if(idSide.add((Identifier)side))
                bool = true;
        } catch (ClassCastException e){
            try {
                addId(idSide, (((Plus)side).leftOperand)); // SOit c'est un plus et on recommence avec partie aguche et partie droite
                addId(idSide, (((Plus)side).rightOperand));
            } catch (ClassCastException e2){
            }
        }
        return bool;
    }

    private void addMap(Map<Identifier, Integer> map, AbstractExpr node){
        try {
            Identifier idLeft = ((Identifier)((Plus)node).getLeftOperand()); // Soit c'est un identifier, on ajoute son nom dans la liste des ID
            boolean exist = false;
            for (Map.Entry<Identifier, Integer> entry : map.entrySet()) {
                Identifier key = entry.getKey();
                if (key.getName().getName().equals(idLeft.getName().getName()))
                    exist = true;
                    map.replace(key, (int)(map.get(key))+1);
            }
            if (!exist)
                map.put(idLeft, 1);
            
        } catch (ClassCastException e){
            try {
            addMap(map, ((Plus)((Plus)node).getLeftOperand()).getLeftOperand()); // SOit c'est un plus et on recommence avec partie aguche et partie droite
            addMap(map, ((Plus)((Plus)node).getLeftOperand()).getRightOperand());
            } catch (ClassCastException e1){

            }
        }
        try {
            Identifier idRight = ((Identifier)((Plus)node).getRightOperand()); // Soit c'est un identifier, on ajoute son nom dans la liste des ID
            boolean exist = false;
            for (Map.Entry<Identifier, Integer> entry : map.entrySet()) {
                Identifier key = entry.getKey();
                if (key.getName().getName().equals(idRight.getName().getName()))
                    exist = true;
                    map.replace(key, (int)(map.get(key))+1);
            }
            if (!exist)
                map.put(idRight, 1);
            
        } catch (ClassCastException e2){
            try {
                addMap(map, ((Plus)((Plus)node).getRightOperand()).getLeftOperand()); // SOit c'est un plus et on recommence avec partie aguche et partie droite
                addMap(map, ((Plus)((Plus)node).getRightOperand()).getRightOperand());
                } catch (ClassCastException e3){
    
                }
        }
    }

    // public boolean factorised(DecacCompiler compiler) {
        // List idLeft = new ArrayList();
        // boolean bool1 = addId(idLeft, leftOperand);
        // boolean bool2 = addId(idLeft, rightOperand);
        // return bool1 || bool2;
    // }

    private void addInst(DecacCompiler compiler, ListInst list, AbstractExpr leftOperand, AbstractExpr rightOperand, Identifier toSimplified){
        Identifier sideway = null;
        try {
            if (!((Identifier)leftOperand).equals(toSimplified)) // Soit c'est un identifier, on ajoute son nom dans la liste des ID
                sideway = (Identifier)leftOperand;
        } catch (ClassCastException e){
            try {
                if (((Identifier)((Plus)leftOperand).getLeftOperand()).equals((Identifier)toSimplified)){
                    Plus plus = new Plus(new Plus(((Plus)leftOperand).getRightOperand(), rightOperand), toSimplified);
                    ((Plus) plus).setType(compiler.environmentType.INT);
                }
                
            } catch (ClassCastException e2){
            }
        }
    }

    // public AbstractInst factoInst(DecacCompiler compiler) {
        // AbstractInst list = null;
        // Map<Identifier, Integer> map = new HashMap();
        // addMap(map, this);

        // if (map.size() == 1) {
        //     for (Map.Entry<Identifier, Integer> entry : map.entrySet()) {
        //         Identifier key = entry.getKey();
        //         Integer value = entry.getValue();
        //         AbstractExpr puissance = new IntLiteral(value);
        //         list.add(new Multiply(key, puissance));
        //     }
        // } else {
        //     AbstractExpr nbGauche = new IntLiteral((Integer)map.values().toArray()[0]);
        //     Multiply multiplyLeft = new Multiply((AbstractExpr)map.keySet().toArray()[0], nbGauche);
        //     ((Multiply) multiplyLeft).setType(compiler.environmentType.INT);
        //     AbstractExpr nbRight = new IntLiteral((Integer)map.values().toArray()[1]);
        //     Multiply multiplyRight = new Multiply((AbstractExpr)map.keySet().toArray()[1], nbRight);
        //     ((Multiply) multiplyRight).setType(compiler.environmentType.INT);
        //     AbstractExpr plus = new Plus(multiplyLeft, multiplyRight);
        //     ((Plus)plus).setType(compiler.environmentType.INT);
        //     list.add(plus);
        //     if (map.size() > 2) {
        //         for (int i = 2; i < map.size(); i++) {
        //             AbstractExpr nb = new IntLiteral((Integer)map.values().toArray()[i]);
        //             Multiply multiply = new Multiply((AbstractExpr)map.keySet().toArray()[i], nb);
        //             Plus operand = new Plus((AbstractExpr) multiply, (AbstractExpr) plus);
        //             ((Plus) operand).setType(compiler.environmentType.INT);
        //             plus = new Plus((AbstractExpr) multiply, (AbstractExpr) plus);
        //             ((Plus) plus).setType(compiler.environmentType.INT);
        //             list.add(operand);
        //         }
        //     }
        // }
        // addInst(compiler, list, leftOperand, rightOperand, toSimplified);
        // list.add(this);
        //return list;
    // }

    @Override
    public boolean collapse() {
        return getLeftOperand().collapse() || getRightOperand().collapse();
    }

    @Override
    public Float collapseFloat() {
        Float rightCollapsedValue = getRightOperand().collapseFloat();
        if(rightCollapsedValue != null && getRightOperand().collapsable()) {
            FloatLiteral newFloat = new FloatLiteral(rightCollapsedValue);
            newFloat.setType(getType());
            setRightOperand(newFloat);
        }
        Float leftCollapsedValue = getLeftOperand().collapseFloat();
        if(leftCollapsedValue != null && getLeftOperand().collapsable()) {
            FloatLiteral newFloat = new FloatLiteral(leftCollapsedValue);
            newFloat.setType(getType());
            setLeftOperand(newFloat);
        }
        if(rightCollapsedValue != null && leftCollapsedValue != null) {
            return rightCollapsedValue + leftCollapsedValue;
        }
        return null;
    }

    @Override
    public Integer collapseInt() {
        Integer rightCollapsedValue = getRightOperand().collapseInt();
        if(rightCollapsedValue != null && getRightOperand().collapsable()) {
            IntLiteral newInt = new IntLiteral(rightCollapsedValue);
            newInt.setType(getType());
            setRightOperand(newInt);
        }
        Integer leftCollapsedValue = getLeftOperand().collapseInt();
        if(leftCollapsedValue != null && getLeftOperand().collapsable()) {
            IntLiteral newInt = new IntLiteral(leftCollapsedValue);
            newInt.setType(getType());
            setLeftOperand(newInt);
        }
        if(rightCollapsedValue != null && leftCollapsedValue != null) {
            return rightCollapsedValue + leftCollapsedValue;
        }
        return null;
    }


}
