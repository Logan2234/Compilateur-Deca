package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.ConvFloat;
import fr.ensimag.deca.tree.Multiply;
import fr.ensimag.deca.tree.Plus;
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
public class TestAndAdvanced {

    final Type BOOLEAN = new BooleanType(null);

    @Mock
    AbstractExpr booleanexpr1;
    @Mock
    AbstractExpr booleanexpr2;    

    DecacCompiler compiler;
    
    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.initMocks(this);
        compiler = new DecacCompiler(null, null);
        when(booleanexpr1.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
        when(booleanexpr2.verifyExpr(compiler, null, null)).thenReturn(BOOLEAN);
    }

    @Test
    public void testBooleanBoolean() throws ContextualError {
        And t = new And(booleanexpr1, booleanexpr2);
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isBoolean());
        // check that the mocks have been called properly.
        verify(booleanexpr1).verifyExpr(compiler, null, null);
        verify(booleanexpr2).verifyExpr(compiler, null, null);
    }

}