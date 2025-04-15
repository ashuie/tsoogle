package assignment;

import java.util.*;

/**
 * A web-index which efficiently stores information about pages. Serialization is done automatically
 * via the superclass "Index" and Java's Serializable interface.
 */
public class WebIndex extends Index {
    /**
     * Needed for Serialization (provided by Index) - don't remove this!
     */
    private static final long serialVersionUID = 1L;

    // You should not need to worry about serialization (just make any other data structures you use
    // here also serializable - the Java standard library data structures already are, for example).

    // maps words to pages and the indices the word appears at for those pages
    private HashMap<String, HashMap<Page, HashSet<Integer>>> map;
    // stores all pages crawled to handle NOT queries
    private HashSet<Page> allPages;

    public WebIndex() {
        map = new HashMap<>();
        allPages = new HashSet<>();
    }

    // Add a page when first being crawled
    public void addPage(Page page) {
        allPages.add(page);
    }

    public void add(String word, Page page, int index) {
        if (word == null || page == null) {
            return;
        }
        if (!map.containsKey(word)) {
            map.put(word, new HashMap<>());
        }
        if (!map.get(word).containsKey(page)) {
            map.get(word).put(page, new HashSet<>());
        }
        // Add the current index the word appears to the hashset for that page
        allPages.add(page);
        map.get(word).get(page).add(index);
    }

    public boolean contains(String words, Page page) {
        if (words == null) {
            return false;
        }
        // Split the phrase by whitespace delimiter
        ArrayList<String> phrase = new ArrayList<>(List.of(words.split(" ")));
        if (page == null || phrase.isEmpty()) {
            return false;
        }
        // Handle simpler single-word case
        String firstWord = phrase.get(0);
        if (phrase.size() == 1) {
            return map.containsKey(firstWord) && map.get(firstWord).containsKey(page);
        }
        // Check that all words in the phrase exist in index
        for (String word : phrase) {
            if (!map.containsKey(word) || !map.get(word).containsKey(page)) {
                return false;
            }
        }
        // Check for the phrase at every index where the first word appears
        HashSet<Integer> indices = map.get(firstWord).get(page);
        for (Integer start : indices) {
            if (searchPhrase(phrase, page, start)) {
                return true;
            }
        }
        return false;
    }

    private boolean searchPhrase(ArrayList<String> phrase, Page page, Integer start) {
        boolean matched = true;
        for (int offset = 0; offset < phrase.size(); offset++) {
            String currWord = phrase.get(offset);
            // Check for contiguous phrase following occurrence of phrase's first word
            if (!map.get(currWord).containsKey(page) ||
                    !map.get(currWord).get(page).contains(start + offset)) {
                matched = false;
                break;
            }
        }
        return matched;
    }

    public HashSet<Page> getPages(String words) {
        if (words == null) {
            return new HashSet<>();
        }
        ArrayList<String> phrase = new ArrayList<>(List.of(words.split(" ")));
        if (phrase.isEmpty()) {
            return new HashSet<>();
        }
        // Handle single-word case separately by returning map key set
        String firstWord = phrase.get(0);
        if (phrase.size() == 1) {
            if (map.containsKey(firstWord)) {
                return new HashSet<>(map.get(firstWord).keySet());
            } else {
                return new HashSet<>();
            }
        }
        // Check that all words in the phrase are indexed
        for (String word : phrase) {
            if (!map.containsKey(word)) {
                return new HashSet<>();
            }
        }
        HashSet<Page> pages  = new HashSet<>(map.get(firstWord).keySet());
        HashSet<Page> searchedPages = new HashSet<>();
        for (Page page : pages) {
            HashSet<Integer> indices = map.get(firstWord).get(page);
            for (Integer start : indices) {
                // Keep track of the pages that successfully contain the phrase
                if (searchPhrase(phrase, page, start)) {
                    searchedPages.add(page);
                }
            }
        }
        return searchedPages;
    }

    public HashSet<Page> excludePages(String word) {
        if (map.isEmpty() || word == null) {
            return new HashSet<>();
        } else if (!map.containsKey(word)) {
            return allPages;
        }
        HashSet<Page> searchedPages = new HashSet<>();
        HashSet<Page> pagesWithWord = new HashSet<>(map.get(word).keySet());
        // Subtract all pages containing the word from the universal set of all pages
        for (Page page : allPages) {
            if (!pagesWithWord.contains(page)) {
                searchedPages.add(page);
            }
        }
        return searchedPages;
    }

    public String toString() {
        return map.keySet() + "\n" + map.toString();
    }
}
