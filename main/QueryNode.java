package assignment;

import java.util.HashSet;

public class QueryNode {
    Token token;
    HashSet<Page> pages;
    QueryNode parent;
    QueryNode left;
    QueryNode right;

    public QueryNode(Token t) {
        this.token = t;
        pages = new HashSet<>();
        parent = null;
        left = null;
        right = null;
    }
}