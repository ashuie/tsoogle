package assignment;
import java.io.Serializable;
import java.net.URL;

/**
 * The Page class holds anything that the QueryEngine returns to the server.  The field and method
 * we provided here is the bare minimum requirement to be a Page - feel free to add anything you
 * want as long as you don't break the getURL method.
 *
 * TODO: Implement this!`
 */
public class Page implements Serializable {
    // The URL the page was located at.;

    private URL url;

    /**
     * Creates a Page with a given URL.
     * @param url The url of the page.
     */
    public Page(URL url) {
        this.url = url;
    }

    /**
     * @return the URL of the page.
     */
    public URL getURL() { return url; }

    public boolean equals(Object o) {
        if (!(o instanceof Page)) {
            return false;
        }
        Page p = (Page)o;
        return url.equals(p.url);
    }

    public int hashCode() {
        return url.getPath().hashCode();
    }

}
