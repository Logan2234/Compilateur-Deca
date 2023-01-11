package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.ConvFloat;
import fr.ensimag.deca.tree.Greater;
import fr.ensimag.deca.tree.ReadFloat;
import fr.ensimag.deca.tree.ReadInt;

/**
 * Test for the Reads nodes using mockito, using @Mock and @Before annotations.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class TestReadAdvanced {

    final Type INT = new IntType(null);
    final Type FLOAT = new FloatType(null);

    DecacCompiler compiler;
    
    @BeforeEach
    public void setup() throws ContextualError {
        MockitoAnnotations.initMocks(this);
        compiler = new DecacCompiler(null, null);
    }

    @Test
    public void testReadInt() throws ContextualError {
        ReadInt t = new ReadInt();
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isInt());
    }

    @Test
    public void testReadFloat() throws ContextualError {
        ReadFloat t = new ReadFloat();
        // check the result
        assertTrue(t.verifyExpr(compiler, null, null).isFloat());
    }
}