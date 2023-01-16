package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.ConvFloat;
import fr.ensimag.deca.tree.LowerOrEqual;

/**
 * Test for the Plus node using mockito, using @Mock and @Before annotations.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class TestLowerOrEqualAdvanced {

    final Type INT = new IntType(null);
    final Type FLOAT = new FloatType(null);

    @Mock
    AbstractExpr intexpr1;
    @Mock
    AbstractExpr intexpr2;
    @Mock
    AbstractExpr floatexpr1;
    @Mock
    AbstractExpr floatexpr2;

    DecacCompiler compiler;
    
    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.initMocks(this);
        compiler = new DecacCompiler(null, null);
        when(intexpr1.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(intexpr2.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(floatexpr1.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
        when(floatexpr2.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
    }

    @Test
    public void testIntInt() throws ContextualError {
        LowerOrEqual t = new LowerOrEqual(intexpr1, intexpr2);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(intexpr2).verifyExpr(compiler, null, null);
    }

    @Test
    public void testIntFloat() throws ContextualError {
        LowerOrEqual t = new LowerOrEqual(intexpr1, floatexpr1);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // ConvFloat should have been inserted on the right side
        assertTrue(t.getLeftOperand() instanceof ConvFloat);
        assertFalse(t.getRightOperand() instanceof ConvFloat);
        // check that the mocks have been called properly.
        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(floatexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void testFloatInt() throws ContextualError {
        LowerOrEqual t = new LowerOrEqual(floatexpr1, intexpr1);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // ConvFloat should have been inserted on the right side
        assertTrue(t.getRightOperand() instanceof ConvFloat);
        assertFalse(t.getLeftOperand() instanceof ConvFloat);
        // check that the mocks have been called properly.
        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(floatexpr1).verifyExpr(compiler, null, null);
    }
}