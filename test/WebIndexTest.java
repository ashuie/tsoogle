package assignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;

public class WebIndexTest {

    WebIndex wi;
    static Page page1;
    static Page page2;
    static Page page3;
    static Page page4;
    static HashSet<Page> universalSet;

    static {
        try {
            page1 = new Page(new File("./test_web_graph/start.html").toURI().toURL());
            page2 = new Page(new File("./test_web_graph/test1.html").toURI().toURL());
            page3 = new Page(new File("./test_web_graph/test2.html").toURI().toURL());
            page4 = new Page(new File("./test_web_graph/test3.html").toURI().toURL());
            universalSet = new HashSet<>(Set.of(page1, page2, page3, page4));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    @BeforeEach
    public void setUp() {
        wi = new WebIndex();
    }

    @Test
    public void addBasicTest() {
        String[] words = {"apple", "banana", "cherry"};
        Page[] pages = {page1, page2, page3};
        for (int i = 0; i < words.length; i++) {
            wi.add(words[i], pages[i], 0);
        }
        Assertions.assertTrue(wi.contains(words[0], page1));
        Assertions.assertTrue(wi.contains(words[1], page2));
        Assertions.assertTrue(wi.contains(words[2], page3));
    }

    @Test
    public void addEdgeCaseTest() {
        // Add identical word
        wi.add("turing", page1, 0);
        wi.add("turing", page1, 1);
        Assertions.assertTrue(wi.contains("turing", page1));
        Assertions.assertTrue(wi.contains("turing turing", page1));

        wi = new WebIndex();

        // Add identical index
        wi.add("test", page2, 0);
        wi.add("test", page2, 0);
        Assertions.assertTrue(wi.contains("test", page2));
        Assertions.assertFalse(wi.contains("test test", page2));
    }

    @Test
    public void getPagesTest() {
        ArrayList<String> words = new ArrayList<>(List.of("league", "of", "legends"));
        ArrayList<Page> pages = new ArrayList<>(List.of(page1, page2, page3, page4));
        for (Page page : pages) {
            for (int i = 0; i < 3; i++) {
                wi.add(words.get(i), page, i);
            }
        }
        wi.add("turing", page1, 3);
        wi.add("thanksgiving", page2, 3);
        wi.add("university", page3, 3);
        Assertions.assertEquals(new HashSet<>(pages), wi.getPages("league of legends"));
        Assertions.assertEquals(new HashSet<>(Set.of(page1)), wi.getPages("league of legends turing"));
        Assertions.assertEquals(new HashSet<>(Set.of(page1)), wi.getPages("turing"));
        Assertions.assertEquals(new HashSet<>(), wi.getPages("word"));
    }

    @Test
    public void getPagesEdgeCaseTest() {
        Assertions.assertEquals(new HashSet<>(), wi.getPages(""));
        wi.add("turing", page1, 0);
        Assertions.assertEquals(new HashSet<>(), wi.getPages("turingg"));
        Assertions.assertEquals(new HashSet<>(), wi.getPages("turing%"));
    }

    @Test
    public void excludePagesTest() {
        wi.add("league", page1, 0);
        wi.add("of", page2, 0);
        wi.add("legends", page3, 0);
        wi.add("league", page4, 0);
        Assertions.assertEquals(universalSet, wi.excludePages("aram"));
        Assertions.assertEquals(new HashSet<>(Set.of(page2, page3)), wi.excludePages("league"));
        Assertions.assertEquals(new HashSet<>(Set.of(page1, page3, page4)), wi.excludePages("of"));
        Assertions.assertEquals(new HashSet<>(Set.of(page1, page2, page4)), wi.excludePages("legends"));
    }
}
