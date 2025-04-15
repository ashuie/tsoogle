package assignment;

import java.util.*;
import java.net.*;
import org.attoparser.simple.*;

/**
 * A markup handler which is called by the Attoparser markup parser as it parses the input;
 * responsible for building the actual web index.
 */
public class CrawlingMarkupHandler extends AbstractSimpleMarkupHandler {

    private static final String LINK = "href";
    private static final HashSet<String> extensions = new HashSet<>(List.of("htm", "html", "txt"));
    private static final HashSet<String> skipElement = new HashSet<>(List.of("style", "script"));

    private WebIndex wi;
    private LinkedList<URL> newURLs;
    private URL currentURL;
    private int place;
    private HashSet<URL> visited;
    private boolean skip;

    public CrawlingMarkupHandler() {
        visited = new HashSet<>();
        place = 0;
        currentURL = null;
        wi = new WebIndex();
        newURLs = new LinkedList<>();
        skip = false;
    }

    /**
    * This method returns the complete index that has been crawled thus far when called.
    */
    public Index getIndex() {
        return wi;
    }

    /**
    * This method returns any new URLs found to the Crawler; upon being called, the set of new URLs
    * should be cleared.
    */
    public List<URL> newURLs() {
        // Copy newURLs to the returned list before clearing
        List<URL> next = new LinkedList<>(newURLs);
        newURLs.clear();
        return next;
    }

    public void setCurrentPage(URL currentURL) {
        this.currentURL = currentURL;
    }

    /*
    * These are some of the methods from AbstractSimpleMarkupHandler.
    * All of its method implementations are NoOps, so we've added some things
    * to do; please remove all the extra printing before you turn in your code.
    * Note: each of these methods defines a line and col param, but you probably
    * don't need those values. You can look at the documentation for the
    * superclass to see all of the handler methods.
    */

    /**
    * Called when the parser first starts reading a document.
    * @param startTimeNanos  the current time (in nanoseconds) when parsing starts
    * @param line            the line of the document where parsing starts
    * @param col             the column of the document where parsing starts
    */
    public void handleDocumentStart(long startTimeNanos, int line, int col) {
        wi.addPage(new Page(currentURL));
        place = 0;
    }

    /**
    * Called when the parser finishes reading a document.
    * @param endTimeNanos    the current time (in nanoseconds) when parsing ends
    * @param totalTimeNanos  the difference between current times at the start
    *                        and end of parsing
    * @param line            the line of the document where parsing ends
    * @param col             the column of the document where the parsing ends
    */
    public void handleDocumentEnd(long endTimeNanos, long totalTimeNanos, int line, int col) {
    }

    /**
    * Called at the start of any tag.
    * @param elementName the element name (such as "div")
    * @param attributes  the element attributes map, or null if it has no attributes
    * @param line        the line in the document where this element appears
    * @param col         the column in the document where this element appears
    */
    public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) {
        // Skip tags like style and script
        if (skipElement.contains(elementName.toLowerCase())) {
            skip = true;
        }
        if (attributes != null) {
            for (String s : attributes.keySet()) {
                if (s.equalsIgnoreCase(LINK)) {
                    // Check for link attributes, then only add valid suffixed files or no suffix
                    String nextLink = attributes.get(s);
                    String suffix = nextLink.substring(nextLink.lastIndexOf('.') + 1);
                    if (extensions.contains(suffix.toLowerCase()) || !nextLink.contains(".")) {
                        try {
                            // Drop fragment identifiers from next link, add only if unique
                            URL rel = new URL(currentURL, nextLink);
                            URL next = new URL(rel.getProtocol(), rel.getHost(), rel.getPort(), rel.getPath());
                            if (!visited.contains(next)) {
                                newURLs.add(next);
                            }
                            visited.add(next);
                        } catch (MalformedURLException e) {
                            System.err.println("[Warning] Malformed URL was not added: " + nextLink);
                        }
                    }
                }
            }
        }
    }

    /**
    * Called at the end of any tag.
    * @param elementName the element name (such as "div").
    * @param line        the line in the document where this element appears.
    * @param col         the column in the document where this element appears.
    */
    public void handleCloseElement(String elementName, int line, int col) {
        skip = false;
    }

    /**
    * Called whenever characters are found inside a tag. Note that the parser is not
    * required to return all characters in the tag in a single chunk. Whitespace is
    * also returned as characters.
    * @param ch      buffer containing characters; do not modify this buffer
    * @param start   location of 1st character in ch
    * @param length  number of characters in ch
    */
    public void handleText(char[] ch, int start, int length, int line, int col) {
        if (!skip) {
            StringBuilder word = new StringBuilder();
            for (int i = start; i < start + length; i++) {
                // Handle distinct words as being separated by a non-alphanumeric character
                if (Character.isLetterOrDigit(ch[i])) {
                    word.append(ch[i]);
                    if (i == start + length - 1) {
                        place++;
                        wi.add(word.toString().toLowerCase(), new Page(currentURL), place);
                    }
                } else if (word.length() > 0) {
                    place++;
                    wi.add(word.toString().toLowerCase(), new Page(currentURL), place);
                    word.setLength(0);
                }
            }
        }
    }
}
