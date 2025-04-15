package assignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;


public class RandomWebQueryTest {

    static final String pagecontentdb = "./randomwebpagecontent.db";
    static HashMap<Page, String> pageContent;
    static ArrayList<String> words;

    String indexdb = "randomwebindex.db";
    WebQueryEngine wqe;


    static {
        try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(pagecontentdb))) {
            pageContent = (HashMap<Page, String>) oin.readObject();
        } catch (Exception e) {
            System.err.println("Error loading db");
        }
        words = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("./words.txt"));
            while (br.ready()) {
                words.add(br.readLine());
            }
        } catch (Exception e) {
            System.err.println("Error loading words");
        }
    }

    @BeforeEach
    public void setUp() throws IOException, ClassNotFoundException {
        wqe = WebQueryEngine.fromIndex(
                (WebIndex) Index.load(indexdb));
    }

    private boolean containsWord(String content, String word) {
        word = word.replace("\"", "");
        int wlen = word.length();
        int clen = content.length();
        for (int i = 0; i <= clen - wlen; i++) {
            String substr = content.substring(i, i + wlen);
            if (substr.hashCode() == word.hashCode() && substr.equals(word)) {
                boolean start = (i == 0 || !Character.isLetterOrDigit(content.charAt(i - 1)));
                boolean end = (i + wlen == clen || !Character.isLetterOrDigit(content.charAt(i + wlen)));
                if (start && end) {
                    return true;
                }
            }
        }
        return false;
    }

    private Collection<Page> getExpectedPages(Predicate<String> contentPredicate) {
        Collection<Page> expected = new HashSet<>();
        for (Page page : pageContent.keySet()) {
            String content = pageContent.get(page);
            if (contentPredicate.test(content)) {
                expected.add(page);
            }
        }
        return expected;
    }

    @Test
    public void QuerySingleWordTest() {
        for (String word : words) {
            Collection<Page> results = wqe.query(word);
            Collection<Page> expected = getExpectedPages(content -> containsWord(content, word));
            compare(results, expected);

        }
    }

    @Test
    public void QueryAndTest() {
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            String word1 = words.get(rand.nextInt(words.size()));
            String word2 = words.get(rand.nextInt(words.size()));
            Collection<Page> results = wqe.query("(" + word1 + " & " + word2 + ")");
            Collection<Page> expected = getExpectedPages(content -> containsWord(content, word1) && containsWord(content, word2));
            Assertions.assertEquals(expected, results);
        }
    }

    @Test
    public void QueryOrTest() {
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) {
            String word1 = words.get(rand.nextInt(words.size()));
            String word2 = words.get(rand.nextInt(words.size()));
            Collection<Page> results = wqe.query("(" + word1 + " | " + word2 + ")");
            Collection<Page> expected = getExpectedPages(content -> containsWord(content, word1) || containsWord(content, word2));
            Assertions.assertEquals(expected, results);
        }
    }

    @Test
    public void QueryNotTest() {
        for (String word : words) {
            Collection<Page> results = wqe.query("!" + word);
            Collection<Page> expected = getExpectedPages(content -> (!containsWord(content, word)));
            Assertions.assertEquals(expected, results);
        }
    }

    private Predicate<String> createPredicate(String[] ops, Random rand, StringBuilder query, int wordct) {
        String firstWord = words.get(rand.nextInt(words.size()));
        query.append(firstWord);
        Predicate<String> predicate = content -> containsWord(content, firstWord);
        int numWords = rand.nextInt(wordct);
        for (int i = 0; i < numWords; i++) {
            String randomWord = words.get(rand.nextInt(words.size()));
            String randomOp = ops[rand.nextInt(ops.length)];
            boolean addNot = Math.random() > 0.5;
            if (addNot) {
                query.insert(0, "(").append(" ").append(randomOp).append(" !")
                        .append(randomWord).append(")");
            } else {
                query.insert(0, "(").append(" ").append(randomOp).append(" ")
                        .append(randomWord).append(")");
            }
            Predicate<String> newPredicate;
            if (addNot) {
                newPredicate = content -> !containsWord(content, randomWord);
            } else {
                newPredicate = content -> containsWord(content, randomWord);
            }
            if (randomOp.equals("&")) {
                predicate = predicate.and(newPredicate);
            } else if (randomOp.equals("|")) {
                predicate = predicate.or(newPredicate);
            }
        }
        return predicate;
    }

    @Test
    public void QueryRandomTest() {
        int iterations = 1000;
        Random rand = new Random();
        for (int i = 0; i < iterations; i++) {
            StringBuilder query = new StringBuilder();
            Predicate<String> predicate = createPredicate(new String[]{"&", "|"}, rand, query, rand.nextInt(10) + 3);
            Collection<Page> results = wqe.query(query.toString());
            Collection<Page> expected = getExpectedPages(predicate);
            Assertions.assertEquals(results, expected);
        }
    }

    @Test
    public void QueryImplicitAndTest() {
        int iterations = 1000;
        Random rand = new Random();
        ArrayList<String> contents = new ArrayList<>(pageContent.values());
        for (int i = 0; i < iterations; i++) {
            String[] text = null;
            while (text == null || text.length < 2) {
                text = contents.get(rand.nextInt(contents.size())).split(" ");
            }
            StringBuilder phrase = new StringBuilder();
            int querySize = Math.min(text.length, rand.nextInt(5) + 2);
            String firstWord = text[rand.nextInt(text.length)];
            phrase.append(firstWord).append(" ");
            Predicate<String> predicate = content -> containsWord(content, firstWord);
            for (int j = 1 ; j < querySize ; j++) {
                String newWord = text[rand.nextInt(text.length)];
                phrase.append(newWord);
                if (j < querySize - 1) {
                    phrase.append(" ");
                }
                Predicate<String> newPredicate = content -> containsWord(content, newWord);
                predicate = predicate.and(newPredicate);
            }
            Collection<Page> results = wqe.query(phrase.toString());
            Collection<Page> expected = getExpectedPages(predicate);
            compare(results, expected);
        }
    }

    @Test
    public void QueryPhraseTest() {
        int iterations = 1000;
        Random rand = new Random();
        ArrayList<String> contents = new ArrayList<>(pageContent.values());
        for (int i = 0; i < iterations; i++) {
            String[] text = null;
            while (text == null || text.length < 2) {
                text = contents.get(rand.nextInt(contents.size())).split(" ");
            }
            int phraseSize = Math.min(text.length, rand.nextInt(3) + 1);
            StringBuilder phrase = new StringBuilder();
            phrase.append("\"");
            int start = 0;
            if (text.length > phraseSize) {
                start = rand.nextInt(text.length - phraseSize);
            }
            for (int j = 0; j < phraseSize; j++) {
                phrase.append(text[start + j]);
                if (j < phraseSize - 1) {
                    phrase.append(" ");
                }
            }
            phrase.append("\"");
            Collection<Page> results = wqe.query(phrase.toString());
            Collection<Page> expected = getExpectedPages(content -> (containsWord(content, phrase.toString())));
            compare(results, expected);
        }
    }

    private void compare(Collection<Page> results, Collection<Page> expected) {
        for (Page p : results) {
            if (!expected.contains(p)) {
                System.out.println("EXPECTED MISSING: " + p.getURL().toString());
            }
        }
        for (Page p : expected) {
            if (!results.contains(p)) {
                System.out.println("RESULTS MISSING: " + p.getURL().toString());
            }
        }
        Assertions.assertEquals(expected, results);
    }
}
