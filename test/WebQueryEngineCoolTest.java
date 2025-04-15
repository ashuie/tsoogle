package assignment;

import java.io.IOException;
import java.util.ArrayList;

public class WebQueryEngineCoolTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        WebQueryEngine we  = WebQueryEngine.fromIndex(
                (WebIndex) Index.load("webindex.db"));
        /* queries
        ((wealth & fame) | happiness)
        ((!"hello world" & b) | c)
        self day shell self fig fresh
         */
        String q = "\"the president\"";
        TokenTree t = new TokenTree(q);
        System.out.println(t);
        //rrayList<Token> tokens = we.tokenize(q);
        //ArrayList<WebQueryEngine.Token> tokens = we.tokenize("(((a & b) | c) & d)");
        //System.out.println(tokens);
       // System.out.println(we.query(q));
        //printTree(we.root, 0);
    }

    public static void printTree(QueryNode node, int depth) {
        if (node == null) return;
        for (int i = 0; i < depth; i++) System.out.print("\t");
        System.out.println(node.token);
        printTree(node.left, depth + 1);
        printTree(node.right, depth + 1);
    }
}