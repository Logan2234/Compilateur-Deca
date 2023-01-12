package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.Not;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

/**
 * Test for the Plus node using mockito, using @Mock and @Before annotations.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class TestNotAdvanced {

    final Type BOOLEAN = new BooleanType(null);

    @Mock
    AbstractExpr booleanexpr;

    DecacCompiler compiler;
    
    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.initMocks(this);
        compiler = new DecacCompiler(null, null);
        when(booleanexpr.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);

    }

    @Test
    public void testBoolean() throws ContextualError {
        Not t = new Not(booleanexpr);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(booleanexpr).verifyExpr(compiler, null, null);
    }
}