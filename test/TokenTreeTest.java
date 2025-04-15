package assignment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TokenTreeTest {

    @Test
    public void tokenTreeBuildTest() {
        String[] sequences = {"word", "(this & that)", "(those | these)",
                "implicit and", "!yes"};
        String[] expected = {"WORD(word)", "AND\n\tWORD(this)\n\tWORD(that)",
                "OR\n\tWORD(those)\n\tWORD(these)", "AND\n\tWORD(implicit)\n\tWORD(and)",
                "NOT\n\tWORD(yes)"};
        for (int i = 0; i < sequences.length; i++) {
            TokenTree tree = new TokenTree(sequences[i]);
            Assertions.assertEquals(expected[i] + "\n", tree.toString());
        }
    }

    @Test
    public void tokenTreeImplicitAndTest() {
        String[] regular = {"((league & of) & legends)", "((league | of) & legends)",
        "(!league & \"of legends\")", "ranked (solo & and) duo", "(ranked & solo)and duo",
        "\"league\" \"of legends\"", "league !of"};
        String[] implicitAnd = {"(league & of) legends", "(league | of) legends",
        "!league \"of legends\"", "((ranked & (solo & and)) & duo)", "(((ranked & solo) & and) & duo)",
        "(\"league\" & \"of legends\")", "(league & !of)"};
        for (int i = 0; i < regular.length; i++) {
            TokenTree regTree = new TokenTree(regular[i]);
            TokenTree implTree = new TokenTree(implicitAnd[i]);
            Assertions.assertEquals(regTree.toString(), implTree.toString());
        }
    }

    @Test
    public void tokenTreeEdgeCaseTest() {
        // And/Or with itself, implicit ands with phrases
        String[] sequences = {"(league & league)", "(league | league)", "league !league",
        "\"league\" \"league\"", "\"league of legends\" \"league of legends\"", "$", "\"@#$%\""};
        String[] expected = {"AND\n\tWORD(league)\n\tWORD(league)",
                "OR\n\tWORD(league)\n\tWORD(league)",
                "AND\n\tWORD(league)\n\tNOT\n\t\tWORD(league)",
                "AND\n\tWORD(league)\n\tWORD(league)",
                "AND\n\tWORD(league of legends)\n\tWORD(league of legends)", "WORD($)",
                "WORD(@#$%)"};
        for (int i = 0; i < sequences.length; i++) {
            TokenTree tree = new TokenTree(sequences[i]);
            Assertions.assertEquals(expected[i] + "\n", tree.toString());
        }

        TokenTree tree = new TokenTree("");
        Assertions.assertEquals("", tree.toString());
    }

    @Test
    public void tokenTreeBadQueryTest() {
        String[] sequences = {"()"};
        //String[] sequences = {"()", "( & )", ")", "("};
        for (int i = 0; i < sequences.length; i++) {
            TokenTree tree = new TokenTree(sequences[i]);
            System.out.println(tree.toString());
        }
    }
}
