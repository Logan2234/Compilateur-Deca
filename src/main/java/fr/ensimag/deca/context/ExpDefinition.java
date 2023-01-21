package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;

/**
 * Definition associated to identifier in expressions.
 *
 * @author gl03
 * @date 01/01/2023
 */
public abstract class ExpDefinition extends Definition {

    public void setOperand(DAddr operand) {
        this.operand = operand;
    }

    public DAddr getOperand() {
        return operand;
    }
    private DAddr operand;

    public ExpDefinition(Type type, Location location) {
        super(type, location);
    }

    @Override
    public boolean spotRelatedDefs() {
        return super.spotRelatedDefs();
    }

}
