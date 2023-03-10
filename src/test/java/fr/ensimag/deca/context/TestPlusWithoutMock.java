//! Not used when testing. See TestPlusAdvanced.java.

package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.optim.CollapseResult;
import fr.ensimag.deca.optim.CollapseValue;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.Plus;
import fr.ensimag.deca.tree.TreeFunction;
import fr.ensimag.ima.pseudocode.GPRegister;

/**
 * Test for the Plus node in a manual way. The same test would be much easier to
 * write using a mock-up framework like Mockito.
 *
 * @see TestPlusPlain to see how the Mockito library can help writing this kind
 *      of tests.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class TestPlusWithoutMock {
    static final Type INT = new IntType(null);
    static final Type FLOAT = new FloatType(null);

    /**
     * Stub usable as a replacement for a real class deriving from AbstractExpr.
     *
     * This would typically be much simpler using Mockito.
     */
    static class DummyIntExpression extends AbstractExpr {
        boolean hasBeenVerified = false;

        @Override
        public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                ClassDefinition currentClass) throws ContextualError {
            hasBeenVerified = true;
            return INT;
        }

        @Override
        public void decompile(IndentPrintStream s) {
            throw new UnsupportedOperationException("Should not be called.");
        }

        @Override
        protected void prettyPrintChildren(PrintStream s, String prefix) {
            throw new UnsupportedOperationException("Should not be called.");
        }

        @Override
        protected void iterChildren(TreeFunction f) {
            throw new UnsupportedOperationException("Should not be called.");
        }

        @Override
        public void codeGenExpr(DecacCompiler compiler, GPRegister register) {
            throw new UnsupportedOperationException("Should not be called.");
        }

        /**
         * Check that the object has been properly used after the test.
         */
        public void checkProperUse() {
            assertTrue(hasBeenVerified, "verifyExpr has not been called");
        }

        @Override
        protected void addUnremovableExpr(List<AbstractExpr> foundMethodCalls) {
            // TODO Auto-generated method stub  
        }

        @Override
        protected void spotUsedVar() {
            // TODO Auto-generated method stub
        }

        @Override
        protected AbstractExpr substitute(Map<ParamDefinition, AbstractExpr> substitutionTable) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected boolean containsField() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public CollapseResult<CollapseValue> collapseExpr() {
            // return nothing ? expect if we find a way to compute methods at compile time...
            return new CollapseResult<CollapseValue>(new CollapseValue(), false);
        }

        @Override
        public boolean isSplitable(DecacCompiler compiler) {
            return false;
        }
    }

    public void testType() throws ContextualError {
        DecacCompiler compiler = new DecacCompiler(null, null);
        DummyIntExpression left = new DummyIntExpression();
        DummyIntExpression right = new DummyIntExpression();
        Plus t = new Plus(left, right);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isInt());
        // check that the dummy expression have been called properly.
        left.checkProperUse();
        right.checkProperUse();
    }

}