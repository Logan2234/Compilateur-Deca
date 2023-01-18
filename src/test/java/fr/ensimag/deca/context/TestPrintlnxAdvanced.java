package fr.ensimag.deca.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.ListExpr;
import fr.ensimag.deca.tree.ListInst;
import fr.ensimag.deca.tree.Print;
import fr.ensimag.deca.tree.Println;

/**
 * Test for the Print nodes using mockito, using @Mock and @Before anPrintations.
 *
 * @author Ensimag
 * @date 01/01/2023
 */
public class TestPrintlnxAdvanced {

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
    @Mock
    ListExpr listexpr;
    @Mock
    ListInst listinst;

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
        listexpr = new ListExpr();
        listinst = new ListInst();
    }

    @Test
    public void testPrintBoolean() throws ContextualError {
        listexpr.add(booleanexpr);
        listexpr.add(booleanexpr);
        listinst.add(new Print(false, listexpr));
        // check the result
        assertThrows(ContextualError.class, () -> {listinst.verifyListInst(compiler, null, null, null);});
        // check that the mocks have been called properly.
        verify(booleanexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testPrintVoid() throws ContextualError {
        listexpr.add(voidexpr);
        listexpr.add(voidexpr);
        listinst.add(new Print(false, listexpr));
        // check the result
        assertThrows(ContextualError.class, () -> {listinst.verifyListInst(compiler, null, null, null);});
        // check that the mocks have been called properly.
        verify(voidexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testPrintNull() throws ContextualError {
        listexpr.add(nullexpr);
        listexpr.add(nullexpr);
        listinst.add(new Print(false, listexpr));
        // check the result
        assertThrows(ContextualError.class, () -> {listinst.verifyListInst(compiler, null, null, null);});
        // check that the mocks have been called properly.
        verify(nullexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testPrintClass() throws ContextualError {
        listexpr.add(classexpr);
        listexpr.add(classexpr);
        listinst.add(new Print(false, listexpr));
        // check the result
        assertThrows(ContextualError.class, () -> {listinst.verifyListInst(compiler, null, null, null);});
        // check that the mocks have been called properly.
        verify(classexpr).verifyExpr(compiler, null, null);
    }

    @Test
    public void testPrint() throws ContextualError {
        listexpr.add(intexpr);
        listexpr.add(floatexpr);
        listexpr.add(stringexpr);
        listinst.add(new Print(false, listexpr));
        // check the result
        assertDoesNotThrow(() -> {listinst.verifyListInst(compiler, null, null, null);});
    }

    @Test
    public void testPrintln() throws ContextualError {
        listexpr.add(intexpr);
        listexpr.add(floatexpr);
        listexpr.add(stringexpr);
        listinst.add(new Println(false, listexpr));
        // check the result
        assertDoesNotThrow(() -> {listinst.verifyListInst(compiler, null, null, null);});
    }

    @Test
    public void testPrintx() throws ContextualError {
        listexpr.add(intexpr);
        listexpr.add(floatexpr);
        listexpr.add(stringexpr);
        listinst.add(new Print(true, listexpr));
        // check the result
        assertDoesNotThrow(() -> {listinst.verifyListInst(compiler, null, null, null);});
    }

    @Test
    public void testPrintlnx() throws ContextualError {
        listexpr.add(intexpr);
        listexpr.add(floatexpr);
        listexpr.add(stringexpr);
        listinst.add(new Println(true, listexpr));
        // check the result
        assertDoesNotThrow(() -> {listinst.verifyListInst(compiler, null, null, null);});
    }
}