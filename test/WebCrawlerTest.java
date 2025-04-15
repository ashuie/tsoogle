package assignment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

public class WebCrawlerTest {

    static Page[] graphPages;
    static HashSet<Page> nonEmptyPages;
    static Page[] treePages;
    static Page textFile;
    static Page noExtensionFile;

    static {
        try {
            Page page1 = new Page(new File("test_web_graph/start.html").toURI().toURL());
            Page page2 = new Page(new File("test_web_graph/test1.html").toURI().toURL());
            Page page3 = new Page(new File("test_web_graph/test2.html").toURI().toURL());
            Page page4 = new Page(new File("test_web_graph/test3.html").toURI().toURL());
            Page page5 = new Page(new File("test_web_graph/test4.html").toURI().toURL());
            Page page6 = new Page(new File("test_web_graph/test5.html").toURI().toURL());
            graphPages = new Page[6];
            graphPages[0] = page1;
            graphPages[1] = page2;
            graphPages[2] = page3;
            graphPages[3] = page4;
            graphPages[4] = page5;
            graphPages[5] = page6;
            nonEmptyPages = new HashSet<>();
            nonEmptyPages.add(page1);
            nonEmptyPages.add(page2);
            nonEmptyPages.add(page3);
            nonEmptyPages.add(page5);
            nonEmptyPages.add(page6);
            treePages = new Page[]{new Page(new File("test_web_tree/root.html").toURI().toURL()),
            new Page(new File("test_web_tree/node1.html").toURI().toURL()),
            new Page(new File("test_web_tree/leaf1.html").toURI().toURL()),
            new Page(new File("test_web_tree/node2.html").toURI().toURL()),
                    new Page(new File("test_web_tree/leaf2.html").toURI().toURL())};
            textFile = new Page(new File("test_web_weirdfiles/textfile.txt").toURI().toURL());
            noExtensionFile = new Page(new File("test_web_weirdfiles/noextension").toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testCrawlCompleteGraph() throws IOException, ClassNotFoundException {
        WebCrawler.indexdb = "testwebindex.db";
        URL start = new URL("file:///C:/Users/NAMEHERE/IdeaProjects/prog7/test_web_graph/start.html");
        WebCrawler.main(new String[]{start.toString()});
        WebIndex wi = (WebIndex) Index.load("testwebindex.db");
        String[][] pageContents = {{"starting", "test web"},
                {"league of legends", "league", "heart", "140", "enemy nexus"},
                {"defy the limits", "we are valorant", "deathmatch"},
                {},
                {"hoojiedoober", "types of queries", "crawl a portion of the web", "ask you to consider"},
                {"recursion", "tostring", "intersectray", "1"}};
        for (int i = 0; i < pageContents.length; i++) {
            for (String si : pageContents[i]) {
                Assertions.assertTrue(wi.contains(si, graphPages[i]));
            }
        }
        Assertions.assertTrue(wi.excludePages("test").contains(graphPages[3]));
        Assertions.assertEquals(new HashSet<>(List.of(graphPages)), wi.excludePages("notaword"));
        Assertions.assertEquals(nonEmptyPages, wi.getPages("click"));
        Assertions.assertEquals(new HashSet<>(List.of(graphPages[3])), wi.excludePages("click"));
    }

    @Test
    public void testCrawlTreeGraph() throws IOException, ClassNotFoundException {
        WebCrawler.indexdb = "testwebindex.db";
        URL start = new URL("file:///C:/Users/NAMEHERE/IdeaProjects/prog7/test_web_tree/root.html");
        WebCrawler.main(new String[]{start.toString()});
        WebIndex wi = (WebIndex) Index.load("testwebindex.db");
        String[][] pageContents = {{"root page", "web"},
                {"world", "hello", "i love league of legends"},
                {"i am", "leaf", "pool party caitlyn"},
                {"kog", "maw", "51", "14", "most popular"},
                {"283", "shiv", "lethal", "fiora", "primary path"}};
        for (int i = 0; i < pageContents.length; i++) {
            for (String si : pageContents[i]) {
                Assertions.assertTrue(wi.contains(si, treePages[i]));
            }
        }
        Assertions.assertEquals(new HashSet<>(List.of(treePages)), wi.excludePages("notaword"));
        Assertions.assertEquals(new HashSet<>(List.of(treePages[0], treePages[1], treePages[3])), wi.excludePages("leaf"));
    }

    @Test
    public void testCrawlNonHTMLFiles() throws IOException, ClassNotFoundException {
        WebCrawler.indexdb = "testwebindex.db";
        URL start = new URL("file:///C:/Users/NAMEHERE/IdeaProjects/prog7/test_web_weirdfiles/textfile.txt");
        WebCrawler.main(new String[]{start.toString()});
        WebIndex wi = (WebIndex) Index.load("testwebindex.db");
        Assertions.assertEquals(new HashSet<>(List.of(textFile, noExtensionFile)), wi.getPages("hello"));
        Assertions.assertEquals(new HashSet<>(List.of(noExtensionFile)), wi.getPages("league of legends"));
    }
}
