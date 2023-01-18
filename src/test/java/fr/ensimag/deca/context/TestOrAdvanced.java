package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.Or;

/**
 * Test for the Or node using mockito, using @Mock and @Before annotations.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class TestOrAdvanced {

    final Type BOOLEAN = new BooleanType(null);
    final Type INT = new IntType(null);
    final Type FLOAT = new FloatType(null);

    @Mock
    AbstractExpr booleanexpr1;
    @Mock
    AbstractExpr booleanexpr2;
    @Mock
    AbstractExpr intexpr1;
    @Mock
    AbstractExpr floatexpr1;

    DecacCompiler compiler;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.initMocks(this);
        compiler = new DecacCompiler(null, null);
        when(booleanexpr1.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
        when(booleanexpr2.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
        when(intexpr1.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(floatexpr1.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
    }

    @Test
    public void testBooleanBoolean() throws ContextualError {
        Or t = new Or(booleanexpr1, booleanexpr2);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(booleanexpr1).verifyExpr(compiler, null, null);
        verify(booleanexpr2).verifyExpr(compiler, null, null);
    }

    @Test
    public void testIntBoolean() throws ContextualError {
        Or t = new Or(intexpr1, booleanexpr1);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(booleanexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void testFloatBoolean() throws ContextualError {
        Or t = new Or(floatexpr1, booleanexpr1);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(floatexpr1).verifyExpr(compiler, null, null);
        verify(booleanexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void tetBooleanInt() throws ContextualError {
        Or t = new Or(booleanexpr1, intexpr1);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(booleanexpr1).verifyExpr(compiler, null, null);
        verify(intexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void testBooleanFloat() throws ContextualError {
        Or t = new Or(booleanexpr1, floatexpr1);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(booleanexpr1).verifyExpr(compiler, null, null);
        verify(floatexpr1).verifyExpr(compiler, null, null);
    }
}