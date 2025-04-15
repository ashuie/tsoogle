package assignment;
import java.util.*;
import java.util.function.Predicate;

/**
 * A query engine which holds an underlying web index and can answer textual queries with a
 * collection of relevant pages.
 *
 */
public class WebQueryEngine {

    private WebIndex wi;

    public static WebQueryEngine fromIndex(WebIndex index) {
        WebQueryEngine wqe =  new WebQueryEngine();
        wqe.setIndex(index);
        return wqe;
    }

    private void setIndex(WebIndex index) {
        wi = index;
    }

    private HashSet<Page> intersect(HashSet<Page> curr, Predicate<Page> condition) {
        // Checks for AND intersection given a condition to either consolidate two page sets or handle leaf
        HashSet<Page> result = new HashSet<>();
        for (Page page : curr) {
            if (condition.test(page)) {
                result.add(page);
            }
        }
        return result;
    }

    private HashSet<Page> leafIntersection(HashSet<Page> curr, String add) {
        // Modifies left query's results to intersect with right child leaf
        return intersect(curr, page -> wi.contains(add, page));
    }

    private HashSet<Page> intersection(HashSet<Page> curr, HashSet<Page> add) {
        // Consolidates results of two subtree queries when right child is not a leaf
        return intersect(curr, add::contains);
    }

    private HashSet<Page> union(HashSet<Page> curr, HashSet<Page> add) {
        // Add both hashsets, which by definition will not contain duplicates
        HashSet<Page> or = new HashSet<>(curr);
        or.addAll(add);
        return or;
    }

    private HashSet<Page> evaluateQuery(QueryNode root) {
        if (root == null) {
            return new HashSet<>();
        }
        // Leaf case to directly retrieve pages matching query
        if (root.left == null && root.right == null) {
            return wi.getPages(root.token.val);
        }
        // Not case to directly retrieve pages matching its child query
        if (root.token.type == Token.Type.NOT && root.left != null && root.left.token.type == Token.Type.WORD) {
            return wi.excludePages(root.left.token.val);
        }
        // Else, evaluate left subtree
        HashSet<Page> left = evaluateQuery(root.left);
        switch (root.token.type) {
            case AND:
                // Handle case of right leaf child, in which current page set can be modified
                if (root.right.left == null && root.right.right == null) {
                    return leafIntersection(left, root.right.token.val);
                } else {
                    return intersection(left, evaluateQuery(root.right));
                }
            case OR:
                // Consolidate both page sets by adding result of evaluating right query
                return union(left, evaluateQuery(root.right));
            default:
                System.err.println("[Error] Unrecognized operation during query evaluation");
                return new HashSet<>();
        }
    }

    /**
     * Returns a Collection of URLs (as Strings) of web pages satisfying the query expression.
     *
     * @param query A query expression.
     * @return A collection of web pages satisfying the query.
     */
    public Collection<Page> query(String query) {
        String seq = query.toLowerCase();
        TokenTree parseTree  = new TokenTree(seq);
        QueryNode root = parseTree.getRoot();
        return evaluateQuery(root);
    }
}
