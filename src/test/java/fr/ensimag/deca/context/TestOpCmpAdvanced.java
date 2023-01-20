package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
import fr.ensimag.deca.tree.ConvFloat;
import fr.ensimag.deca.tree.Equals;

/**
 * Test for the Equals node using mockito, using @Mock and @Before annotations.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class TestOpCmpAdvanced {
    final Type INT = new IntType(null);
    final Type FLOAT = new FloatType(null);
    final Type BOOLEAN = new BooleanType(null);
    final Type CLASS = new ClassType(null);
    final Type NULL = new NullType(null);

    @Mock
    AbstractExpr intexpr1;
    @Mock
    AbstractExpr intexpr2;
    @Mock
    AbstractExpr floatexpr1;
    @Mock
    AbstractExpr floatexpr2;
    @Mock
    AbstractExpr booleanexpr1;
    @Mock
    AbstractExpr booleanexpr2;
    @Mock
    AbstractExpr classexpr1;
    @Mock
    AbstractExpr classexpr2;
    @Mock
    AbstractExpr nullexpr1;
    @Mock
    AbstractExpr nullexpr2;

    DecacCompiler compiler;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.initMocks(this);
        compiler = new DecacCompiler(null, null);
        when(intexpr1.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(intexpr2.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(floatexpr1.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
        when(floatexpr2.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
        when(booleanexpr1.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
        when(booleanexpr2.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
        when(classexpr1.verifyExpr(compiler, null, null)).thenReturn(CLASS);
        when(classexpr2.verifyExpr(compiler, null, null)).thenReturn(CLASS);
        when(nullexpr1.verifyExpr(compiler, null, null)).thenReturn(NULL);
        when(nullexpr2.verifyExpr(compiler, null, null)).thenReturn(NULL);
    }

    @Test
    public void testIntInt() throws ContextualError {
        Equals t = new Equals(intexpr1, intexpr2);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(intexpr2).verifyExpr(compiler, null, null);
    }

    @Test
    public void testIntFloat() throws ContextualError {
        Equals t = new Equals(intexpr1, floatexpr1);
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
        Equals t = new Equals(floatexpr1, intexpr1);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // ConvFloat should have been inserted on the right side
        assertTrue(t.getRightOperand() instanceof ConvFloat);
        assertFalse(t.getLeftOperand() instanceof ConvFloat);
        // check that the mocks have been called properly.
        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(floatexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void testBooleanBoolean() throws ContextualError {
        Equals t = new Equals(booleanexpr1, booleanexpr2);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(booleanexpr1).verifyExpr(compiler, null, null);
        verify(booleanexpr2).verifyExpr(compiler, null, null);
    }

    @Test
    public void testBooleanInt() throws ContextualError {
        Equals t = new Equals(booleanexpr1, intexpr1);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(booleanexpr1).verifyExpr(compiler, null, null);
        verify(intexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void testIntNull() throws ContextualError {
        Equals t = new Equals(intexpr1, nullexpr1);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(intexpr1).verifyExpr(compiler, null, null);
        verify(nullexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void testNullFloat() throws ContextualError {
        Equals t = new Equals(nullexpr1, floatexpr1);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(nullexpr1).verifyExpr(compiler, null, null);
        verify(floatexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void testBooleanFloat() throws ContextualError {
        Equals t = new Equals(booleanexpr1, floatexpr1);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(booleanexpr1).verifyExpr(compiler, null, null);
        verify(floatexpr1).verifyExpr(compiler, null, null);
    }

    @Test
    public void testClassClass() throws ContextualError {
        Equals t = new Equals(classexpr1, classexpr2);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(classexpr1).verifyExpr(compiler, null, null);
        verify(classexpr2).verifyExpr(compiler, null, null);
    }

    @Test
    public void testnullnull() throws ContextualError {
        Equals t = new Equals(nullexpr1, nullexpr2);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(nullexpr1).verifyExpr(compiler, null, null);
        verify(nullexpr2).verifyExpr(compiler, null, null);
    }

    @Test
    public void testnullclass() throws ContextualError {
        Equals t = new Equals(nullexpr1, classexpr1);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(nullexpr1).verifyExpr(compiler, null, null);
        verify(classexpr1).verifyExpr(compiler, null, null);
    }
}