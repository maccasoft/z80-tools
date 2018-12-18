package nl.grauw.glass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.LineNumberReader;
import java.io.StringReader;

import org.junit.Test;

import nl.grauw.glass.Parser.SyntaxError;
import nl.grauw.glass.expressions.CharacterLiteral;
import nl.grauw.glass.expressions.Expression;
import nl.grauw.glass.expressions.ExpressionBuilder.ExpressionError;
import nl.grauw.glass.expressions.Flag;
import nl.grauw.glass.expressions.IntegerLiteral;

public class ParserTest {

    @Test
    public void testLabel() {
        assertEquals("test_label1", parse("test_label1:").getLabel());
    }

    @Test
    public void testLabelNoColon() {
        assertEquals("test_label1", parse("test_label1").getLabel());
    }

    @Test
    public void testLabelIndented() {
        assertEquals("test_label", parse(" test_label:").getLabel());
    }

    @Test
    public void testLabelIndentedWithMnemonic() {
        assertEquals("test_label", parse(" test_label:exx").getLabel());
        assertEquals("exx", parse(" test_label:exx").getMnemonic());
    }

    @Test
    public void testMnemonic() {
        assertEquals("exx", parse(" exx").getMnemonic());
    }

    @Test
    public void testArguments() {
        assertTrue(parse("\tcp 0H").getArguments() instanceof IntegerLiteral);
    }

    @Test
    public void testComment() {
        assertEquals("test comment", parse(";test comment").getComment());
    }

    @Test
    public void testParser1() {
        assertEquals(";test comment", parse(" ;test comment").toString());
    }

    @Test
    public void testParser2() {
        assertEquals("test_label1: ;test", parse("test_label1:;test").toString());
    }

    @Test
    public void testParser3() {
        assertEquals("test_label1: ;test", parse("test_label1;test").toString());
    }

    @Test
    public void testParser4() {
        assertEquals("test_label1: exx ;test", parse("test_label1:exx;test").toString());
    }

    @Test
    public void testParser5() {
        assertEquals("test_label1: push af ;test", parse("test_label1: push af ;test").toString());
    }

    @Test
    public void testParser6() {
        assertEquals("test_label1: ex af, af' ;test", parse("test_label1: ex af,af';test").toString());
    }

    @Test
    public void testCharacterLiteral() {
        assertEquals('x', ((CharacterLiteral) parseExpression("'x'")).getCharacter());
    }

    @Test
    public void testCharacterLiteralEscape() {
        assertEquals('"', ((CharacterLiteral) parseExpression("'\\\"'")).getCharacter());
    }

    @Test(expected = SyntaxError.class)
    public void testCharacterLiteralTooLong() {
        parse("'xx'");
    }

    @Test(expected = SyntaxError.class)
    public void testCharacterLiteralTooShort() {
        parse("''");
    }

    @Test(expected = SyntaxError.class)
    public void testCharacterLiteralUnclosed() {
        parse("'");
    }

    @Test(expected = SyntaxError.class)
    public void testCharacterLiteralUnclosedEscape() {
        parse("'\\");
    }

    @Test(expected = SyntaxError.class)
    public void testHexNumberTooShort() {
        parseExpression("0x");
    }

    @Test(expected = ExpressionError.class)
    public void testHexNumberWrong() {
        parseExpression("003x0");
    }

    @Test(expected = ExpressionError.class)
    public void testHexNumberWrong2() {
        parseExpression("0x0x0");
    }

    @Test(expected = ExpressionError.class)
    public void testHexNumberWrong3() {
        parseExpression("3x0");
    }

    @Test
    public void testNumber() {
        assertEquals(127, parseExpression("127").getInteger());
        assertEquals(4095, parseExpression("0FFFH").getInteger());
        assertEquals(4095, parseExpression("#0FFF").getInteger());
        assertEquals(4095, parseExpression("$0FFF").getInteger());
        assertEquals(171, parseExpression("10101011B").getInteger());
        assertEquals(171, parseExpression("%10101011").getInteger());
        assertEquals(255, parseExpression("0xFF").getInteger());
        assertEquals(50, parseExpression("0X032").getInteger());
    }

    @Test
    public void testFlag() {
        assertEquals(Flag.NZ, parseExpression("nz").getFlag());
        assertEquals(Flag.Z, parseExpression("z").getFlag());
        assertEquals(Flag.NC, parseExpression("nc").getFlag());
        assertEquals(Flag.C, parseExpression("c").getFlag());
        assertEquals(Flag.PO, parseExpression("po").getFlag());
        assertEquals(Flag.PE, parseExpression("pe").getFlag());
        assertEquals(Flag.P, parseExpression("p").getFlag());
        assertEquals(Flag.M, parseExpression("m").getFlag());
    }

    @Test
    public void testFlagNegate() {
        assertEquals(Flag.Z, parseExpression("!nz").getFlag());
        assertEquals(Flag.NZ, parseExpression("!z").getFlag());
        assertEquals(Flag.C, parseExpression("!nc").getFlag());
        assertEquals(Flag.NC, parseExpression("!c").getFlag());
        assertEquals(Flag.PE, parseExpression("!po").getFlag());
        assertEquals(Flag.PO, parseExpression("!pe").getFlag());
        assertEquals(Flag.M, parseExpression("!p").getFlag());
        assertEquals(Flag.P, parseExpression("!m").getFlag());
    }

    public Line parse(String text) {
        LineNumberReader reader = new LineNumberReader(new StringReader(text));
        return new Parser().parse(reader, new Scope(), null);
    }

    public Expression parseExpression(String text) {
        return parse(" test " + text).getArguments();
    }

}
