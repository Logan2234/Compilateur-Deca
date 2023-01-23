package fr.ensimag.deca.optim;

public class CollapseResult<T> {
    private final boolean couldCollapse;
    private final T collapsedValue;

    public CollapseResult(T result, boolean couldCollapse) {
        this.couldCollapse = couldCollapse;
        this.collapsedValue = result;
    }

    public boolean couldCollapse() {
        return couldCollapse;
    }

    public T getResult() {
        return collapsedValue;
    }
}

