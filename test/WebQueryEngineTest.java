package assignment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;

public class WebQueryEngineTest {

    static WebQueryEngine wqeg;
    static WebQueryEngine wqet;
    static HashSet<Page> pagesg;
    static HashSet<Page> pagest;
    static Page start;
    static Page test1;
    static Page test2;
    static Page test3;
    static Page test4;
    static Page test5;
    static Page root;
    static Page node1;
    static Page node2;
    static Page leaf1;
    static Page leaf2;

    static {
        try {
            start = new Page(new File("test_web_graph/start.html").toURI().toURL());
            test1 = new Page(new File("test_web_graph/test1.html").toURI().toURL());
            test2 = new Page(new File("test_web_graph/test2.html").toURI().toURL());
            test3 = new Page(new File("test_web_graph/test3.html").toURI().toURL());
            test4 = new Page((new File("test_web_graph/test4.html").toURI().toURL()));
            test5 = new Page((new File("test_web_graph/test5.html").toURI().toURL()));
            pagesg = new HashSet<>(List.of(start, test1, test2, test3, test4, test5));
            root = new Page(new File("test_web_tree/root.html").toURI().toURL());
            node1 = new Page(new File("test_web_tree/node1.html").toURI().toURL());
            leaf1 = new Page(new File("test_web_tree/leaf1.html").toURI().toURL());
            node2 = new Page(new File("test_web_tree/node2.html").toURI().toURL());
            leaf2 = new Page(new File("test_web_tree/leaf2.html").toURI().toURL());
            pagest = new HashSet<>(List.of(root, node1, leaf1, node2, leaf2));
            WebCrawler.indexdb = "testwebindex.db";
            WebCrawler.main(new String[]{"file:///C:/Users/ashle/IdeaProjects/prog7/test_web_graph/start.html"});
            wqeg = WebQueryEngine.fromIndex((WebIndex) Index.load("testwebindex.db"));
            WebCrawler.main(new String[]{"file:///C:/Users/ashle/IdeaProjects/prog7/test_web_tree/root.html"});
            wqet = WebQueryEngine.fromIndex((WebIndex) Index.load("testwebindex.db"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void basicQueryTest() {
        Assertions.assertEquals(new HashSet<>(List.of(test2, test5)), wqeg.query("with"));
        Assertions.assertEquals(new HashSet<>(List.of(test1, test2, test3)),
                wqeg.query("((\"league of legends\" | \"valorant\") | !assignment) !web !stack"));
        Assertions.assertEquals(new HashSet<>(List.of(test2)), wqeg.query("defy the limits"));
        Assertions.assertEquals(new HashSet<>(List.of(test1)),
                wqeg.query("\"five powerful champions face off to destroy\""));

        Assertions.assertEquals(new HashSet<>(List.of(root)), wqet.query("(root | page)"));
        Assertions.assertEquals(new HashSet<>(List.of(leaf1, leaf2)), wqet.query("(\"pool party caitlyn\" | rageblade))"));
        Assertions.assertEquals(new HashSet<>(pagest), wqet.query("(!nonchalant & !mysterious)"));
    }

    @Test
    public void edgeCaseQueryTest() {
        Assertions.assertEquals(new HashSet<>(), wqeg.query(""));
        Assertions.assertEquals(new HashSet<>(), wqet.query(""));
        Assertions.assertEquals(new HashSet<>(), wqeg.query("喵"));
        Assertions.assertEquals(wqet.query("(bottom & champion)"), wqet.query("(bottom&champion)"));
        Assertions.assertEquals(wqeg.query("((\"league\" & legends) | valorant)"), wqeg.query("((\"league\"&legends)|valorant)"));
        Assertions.assertEquals(new HashSet<>(List.of(test1)), wqeg.query("\"   league       \""));
        Assertions.assertEquals(new HashSet<>(List.of(test1)), wqeg.query("\"   league   of      legends      \""));
    }
}
