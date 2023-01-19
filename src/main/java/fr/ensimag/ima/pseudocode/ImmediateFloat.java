package fr.ensimag.ima.pseudocode;

/**
 * Immediate operand containing a float value.
 * 
 * @author Ensimag
 * @date 01/01/2023
 */
public class ImmediateFloat extends DVal {
    private float value;

    public ImmediateFloat(float value) {
        super();
        this.value = value;
    }

    @Override
    public String toString() {
        return "#" + Float.toHexString(value);
    }

    @Override
    public boolean isFloatImmediate() {
        return true;
    }

    @Override
    public ImmediateFloat asFloatImmediate() {
        return this;
    }
}
