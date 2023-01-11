package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.UnaryMinus;

/**
 * Test for the Modulo node using mockito, using @Mock and @Before annotations.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class TestUnaryMinusAdvanced {

    final Type INT = new IntType(null);
    final Type FLOAT = new FloatType(null);

    @Mock
    AbstractExpr intexpr1;
    @Mock
    AbstractExpr intexpr2;

    DecacCompiler compiler;
    
    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.initMocks(this);
        compiler = new DecacCompiler(null, null);
        when(intexpr1.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(intexpr2.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
    }

    @Test
    public void testInt() throws ContextualError {
        UnaryMinus t = new UnaryMinus(intexpr1);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isInt());
        // check that the mocks have been called properly.
        verify(intexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void testFloat() throws ContextualError {
        UnaryMinus t = new UnaryMinus(intexpr2);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isFloat());
        // check that the mocks have been called properly.
        verify(intexpr2).verifyExpr(compiler, null, null);
    }
}