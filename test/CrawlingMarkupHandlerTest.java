package assignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.util.*;

public class CrawlingMarkupHandlerTest {

    static final File start = new File("./test_web/start.html");
    static final File test1 = new File("./test_web/test1.html");
    static final String[] testString = {"I", "love", "League", "of", "Legends"};

    private CrawlingMarkupHandler handler;
    private URL startURL;
    private URL test1URL;
    private Page startPage;

    @BeforeEach
    public void setUp() {
        handler = new CrawlingMarkupHandler();
        try {
            startURL = start.toURI().toURL();
            test1URL = test1.toURI().toURL();
            startPage = new Page(startURL);
        } catch (MalformedURLException e) {
            System.err.println("[Warning] Malformed URL");
        }
    }

    @Test
    public void handleTextBasicTest() {
        handler.setCurrentPage(startURL);
        String[][] testTexts = {{"this", "is", "a", "test"}, {"cs314h", "is", "cool"}};
        for (String[] text : testTexts) {
            WebIndex wi = (WebIndex) handler.getIndex();
            for (String t : text) {
                handler.handleText(t.toCharArray(), 0, t.length(), 0,0);
                wi = (WebIndex) handler.getIndex();
                Assertions.assertTrue(wi.contains(t, startPage));
            }
            Assertions.assertTrue(wi.contains("this is a test", startPage));
        }
    }

    @Test
    public void handleTextEdgeCaseTest() {
        handler.setCurrentPage(startURL);
        String[] testText = {"&hello^world", "$#$@#$", ""};
        for (String t : testText) {
            handler.handleText(t.toCharArray(), 0, t.length(), 0,0);
        }
        WebIndex wi = (WebIndex) handler.getIndex();
        Assertions.assertTrue(wi.contains("hello world", startPage));
    }

    @Test
    public void handleOpenElementBasicTest() {
        handler.setCurrentPage(startURL);
        handler.handleOpenElement("a", new HashMap<>(Map.of("href", test1URL.toString())), 0, 0);
        Assertions.assertTrue(handler.newURLs().contains(test1URL));
    }

    @Test
    public void handleOpenElementRevisitLinkTest() {
        handler.setCurrentPage(startURL);
        handler.handleOpenElement("a", new HashMap<>(), 0, 0);
        Assertions.assertTrue(handler.newURLs().isEmpty());
        handler.handleOpenElement("a", new HashMap<>(Map.of("href", test1URL.toString())), 0, 0);
        handler.newURLs();
        handler.handleOpenElement("a", new HashMap<>(Map.of("href", test1URL.toString())), 0, 0);
        Assertions.assertTrue(handler.newURLs().isEmpty());
    }

    @Test
    public void handleOpenElementSkipTagTest() {
        handler.setCurrentPage(startURL);
        handler.handleOpenElement("style", new HashMap<>(), 0, 0);
        WebIndex wi = (WebIndex) handler.getIndex();
        for (String t : testString) {
            handler.handleText(t.toCharArray(), 0, t.length(), 0,0);
            wi = (WebIndex) handler.getIndex();
            Assertions.assertFalse(wi.contains(t, startPage));
        }
        Assertions.assertFalse(wi.contains("I love league of legends", startPage));
    }
}
