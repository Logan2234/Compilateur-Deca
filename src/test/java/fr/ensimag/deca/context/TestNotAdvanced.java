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
import fr.ensimag.deca.tree.Not;

/**
 * Test for the Plus node using mockito, using @Mock and @Before annotations.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class TestNotAdvanced {

    final Type INT = new IntType(null);
    final Type FLOAT = new FloatType(null);
    final Type BOOLEAN = new BooleanType(null);
    final Type STRING = new StringType(null);
    final Type NULL = new NullType(null);
    final Type VOID = new VoidType(null);
    final Type CLASS = new ClassType(null);

    @Mock
    AbstractExpr intexpr;
    @Mock
    AbstractExpr floatexpr;
    @Mock
    AbstractExpr booleanexpr;
    @Mock
    AbstractExpr stringexpr;
    @Mock
    AbstractExpr nullexpr;
    @Mock
    AbstractExpr voidexpr;
    @Mock
    AbstractExpr classexpr;

    DecacCompiler compiler;

    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.initMocks(this);
        compiler = new DecacCompiler(null, null);
        when(booleanexpr.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
        when(intexpr.verifyExpr(compiler, null, null)).thenReturn(INT);
        when(floatexpr.verifyExpr(compiler, null, null)).thenReturn(FLOAT);
        when(stringexpr.verifyExpr(compiler, null, null)).thenReturn(STRING);
        when(nullexpr.verifyExpr(compiler, null, null)).thenReturn(NULL);
        when(voidexpr.verifyExpr(compiler, null, null)).thenReturn(VOID);
        when(classexpr.verifyExpr(compiler, null, null)).thenReturn(CLASS);
    }

    @Test
    public void testBoolean() throws ContextualError {
        Not t = new Not(booleanexpr);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(booleanexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testInt() throws ContextualError {
        Not t = new Not(intexpr);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null).isBoolean();
        });
        // check that the mocks have been called properly.
        verify(intexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testFloat() throws ContextualError {
        Not t = new Not(floatexpr);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(floatexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testString() throws ContextualError {
        Not t = new Not(stringexpr);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(stringexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testNull() throws ContextualError {
        Not t = new Not(nullexpr);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(nullexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testVoid() throws ContextualError {
        Not t = new Not(voidexpr);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(voidexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testClass() throws ContextualError {
        Not t = new Not(classexpr);
        // check the result
        assertThrows(ContextualError.class, () -> {
            t.verifyExpr(compiler, null, null);
        });
        // check that the mocks have been called properly.
        verify(classexpr).verifyExpr(compiler, null, null);
    }
}