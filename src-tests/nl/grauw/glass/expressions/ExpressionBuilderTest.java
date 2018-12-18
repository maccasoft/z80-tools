package nl.grauw.glass.expressions;

import static org.junit.Assert.assertEquals;

import java.io.LineNumberReader;
import java.io.StringReader;

import org.junit.Test;

import nl.grauw.glass.Line;
import nl.grauw.glass.Parser;
import nl.grauw.glass.Parser.SyntaxError;
import nl.grauw.glass.Scope;
import nl.grauw.glass.expressions.ExpressionBuilder.ExpressionError;

public class ExpressionBuilderTest {

    @Test
    public void testSingleValue() {
        assertEquals("a", parse("a"));
    }

    @Test
    public void testAddition() {
        assertEquals("{a + 1H}", parse("a + 1H"));
    }

    @Test
    public void testAddition2() {
        assertEquals("{{a + 1H} + 2H}", parse("a + 1H + 2H"));
    }

    @Test
    public void testPrecedence() {
        assertEquals("{a + {1H * 2H}}", parse("a + 1H * 2H"));
    }

    @Test
    public void testPrecedence2() {
        assertEquals("{{a + {1H * 2H}} + b}", parse("a + 1H * 2H + b"));
    }

    @Test
    public void testGrouping() {
        assertEquals("{a + {({1H + 2H}) * 3H}}", parse("a + (1H + 2H) * 3H"));
    }

    @Test
    public void testGrouping2() {
        assertEquals("{{10H + ({15H * ({5H - 2H})})} + 4H}", parse("10H + (15H * (5H - 2H)) + 4H"));
    }

    @Test(expected = ExpressionError.class)
    public void testGrouping3() {
        parse("10H + (15H * (5H - 2H) + 4H");
    }

    @Test(expected = ExpressionError.class)
    public void testGrouping4() {
        parse("10H + 15H * (5H - 2H)) + 4H");
    }

    @Test
    public void testMember() {
        assertEquals("{($).member}", parse("($).member"));
    }

    @Test
    public void testMemberCombinedIdentifier() {
        assertEquals("object.member", parse("object.member"));
    }

    @Test
    public void testIndex() {
        assertEquals("{({4H, 5H})[0H]}", parse("(4H, 5H)[0H]"));
    }

    @Test
    public void testIndexPrecedence() {
        assertEquals("{{$.member}[0H]}", parse("$.member[0]"));
    }

    @Test
    public void testTernaryIfElse() {
        assertEquals("{a ? 1H : 2H}", parse("a ? 1H : 2H"));
    }

    @Test
    public void testTernaryIfElseNested1() {
        assertEquals("{a ? 1H : {b ? 2H : 3H}}", parse("a ? 1H : b ? 2H : 3H"));
    }

    @Test
    public void testTernaryIfElseNested2() {
        assertEquals("{a ? {b ? 1H : 2H} : 3H}", parse("a ? b ? 1H : 2H : 3H"));
    }

    @Test
    public void testTernaryIfElseNested3() {
        assertEquals("{a ? {b ? 1H : 2H} : 3H}", parse("a ? b ? 1H : 2H : 3H"));
    }

    @Test
    public void testTernaryIfElseNested4() {
        assertEquals("{{ANN 1H}, {{ANN {a ? {b ? {2H + 3H} : {{c < d} ? 4H : {5H - 6H}}} : 7H}}, {e ? 8H : 9H}}}",
            parse("ANN 1H, ANN a ? b ? 2H + 3H : c < d ? 4H : 5H - 6H : 7H, e ? 8H : 9H"));
    }

    @Test(expected = ExpressionError.class)
    public void testTernaryIfWithoutElse() {
        parse("a ? 1H");
    }

    @Test(expected = ExpressionError.class)
    public void testTernaryElseWithoutIf() {
        parse("a : b");
    }

    @Test
    public void testTernaryIfElseHigherPrecedence() {
        assertEquals("{{a < 1H} ? {x + 1H} : {y + 2H}}", parse("a < 1H ? x + 1H : y + 2H"));
    }

    @Test
    public void testTernaryIfElseLowerPrecedence() {
        assertEquals("{0H, {{a ? 1H : 2H}, 3H}}", parse("0H, a ? 1H : 2H, 3H"));
    }

    @Test(expected = ExpressionError.class)
    public void testTernaryIfElseLowerPrecedenceNegative() {
        parse("a ? 1H, 2H : 3H");
    }

    @Test
    public void testTernaryIfElseLowerPrecedenceGroup() {
        assertEquals("{a ? ({1H, 2H}) : 3H}", parse("a ? (1H, 2H) : 3H"));
    }

    @Test
    public void testSequence() {
        assertEquals("{a, 1H}", parse("a, 1H"));
    }

    @Test
    public void testSequence2() {
        assertEquals("{{a + 1H}, {b + 2H}}", parse("a + 1H, b + 2H"));
    }

    @Test
    public void testSequence3() {
        assertEquals("{a, {{1H + 2H}, {3H + 4H}}}", parse("a, 1H + 2H, 3H + 4H"));
    }

    @Test
    public void testSequenceInGroup() {
        assertEquals("{1H + {({a, {2H, 3H}}) * b}}", parse("1H + (a, 2H, 3H) * b"));
    }

    @Test
    public void testSequenceWithDoubleGroup() {
        assertEquals("{a, {{({10H + 15H}) * ({5H - 2H})} + 4H}}", parse("a, (10H + 15H) * (5H - 2H) + 4H"));
    }

    @Test
    public void testAnnotation() {
        assertEquals("{a 1H}", parse("a 1H"));
    }

    @Test
    public void testAnnotationTwice() {
        assertEquals("{a {b 1H}}", parse("a b 1H"));
    }

    @Test
    public void testAnnotationGroup() {
        assertEquals("{a (1H)}", parse("a (1H)"));
    }

    @Test
    public void testAnnotationNot() {
        assertEquals("{a !1H}", parse("a !1H"));
    }

    @Test
    public void testAnnotationComplement() {
        assertEquals("{a ~1H}", parse("a ~1H"));
    }

    @Test
    public void testAnnotationSubtract() {
        assertEquals("{a {1H - 2H}}", parse("a 1H - 2H"));
    }

    @Test
    public void testAnnotationLogicalOr() {
        assertEquals("{a {1H || 2H}}", parse("a 1H || 2H"));
    }

    @Test
    public void testAnnotationSequence() {
        assertEquals("{{a 1H}, {b 2H}}", parse("a 1H, b 2H"));
    }

    @Test
    public void testAnnotationInGroup() {
        assertEquals("{a {1H || ({b 2H})}}", parse("a 1H || (b 2H)"));
    }

    @Test(expected = ExpressionError.class)
    public void testAnnotationInTheMiddle() {
        parse("a 1H || b 2H");
    }

    @Test(expected = ExpressionError.class)
    public void testAnnotationNotAnIdentifier() {
        parse("0 1H");
    }

    @Test(expected = ExpressionError.class)
    public void testAnnotationNotAnIdentifier2() {
        parse("a 0 1H");
    }

    @Test
    public void testMultiline() {
        assertEquals("{a + 1H}", parse("a +\n1H"));
    }

    @Test
    public void testMultiline2() {
        assertEquals("{a, 1H}", parse("a, ;\n 1H"));
    }

    @Test
    public void testMultiline3() {
        assertEquals("a", parse("a \n + 1H"));
    }

    @Test(expected = ExpressionError.class)
    public void testMultilineLabel() {
        assertEquals(null, parse("a +\ntest: 1H"));
    }

    @Test(expected = SyntaxError.class)
    public void testIncomplete() {
        assertEquals(null, parse("a,"));
    }

    public String parse(String text) {
        LineNumberReader reader = new LineNumberReader(new StringReader(" test " + text));
        Line line = new Parser().parse(reader, new Scope(), null);
        return line.getArguments().toDebugString();
    }

}
