package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.BooleanLiteral;
import fr.ensimag.deca.tree.FloatLiteral;
import fr.ensimag.deca.tree.IntLiteral;
import fr.ensimag.deca.tree.Null;
import fr.ensimag.deca.tree.StringLiteral;

/**
 * Test for the Literals nodes using mockito, using @Mock and @Before annotations.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class TestLiteralsAdvanced {

    DecacCompiler compiler;
    
    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.initMocks(this);
        compiler = new DecacCompiler(null, null);
    }

    @Test
    public void testTrue() throws ContextualError {
        BooleanLiteral t = new BooleanLiteral(true);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
    }

    @Test
    public void testFalse() throws ContextualError {
        BooleanLiteral t = new BooleanLiteral(false);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
    }

    @Test
    public void testFloat() throws ContextualError {
        FloatLiteral t = new FloatLiteral(2.0f);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isFloat());
    }

    @Test
    public void testString() throws ContextualError {
        StringLiteral t = new StringLiteral("Test");
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isString());
    }

    @Test
    public void testInt() throws ContextualError {
        IntLiteral t = new IntLiteral(2);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isInt());
    }

    @Test
    public void testNull() throws ContextualError {
        Null t = new Null();
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isNull());
    }
}