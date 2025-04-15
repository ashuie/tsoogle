package assignment;

import java.util.*;

public class TokenTree {

    static final Set<Character> ops = Set.of('&', '|', '!');
    static final Set<Character> parens = Set.of('(', ')');
    static final Set<Token.Type> queryStart = Set.of(Token.Type.WORD, Token.Type.NOT, Token.Type.LEFTPAREN);

    // Passes integers by reference when tokenizing
    public static class IntegerWrapper {
        int val;

        public IntegerWrapper(int val) {
            this.val = val;
        }
    }

    private QueryNode root;
    private Stack<Token> tokenStack;

    public TokenTree(String seq) {
        ArrayList<Token> tokens = tokenize(seq);
        tokenStack = new Stack<>();
        // Push tokens onto a stack in reverse
        for (int i = tokens.size() - 1; i >= 0; i--) {
            tokenStack.push(tokens.get(i));
        }
        root = buildTree();
    }

    private ArrayList<Token> tokenize(String stream) {
        IntegerWrapper index = new IntegerWrapper(0);
        ArrayList<Token> queryTokens = new ArrayList<>();
        while (index.val < stream.length()) {
            ArrayList<Token> t = getToken(stream, index);
            if (t != null) {
                queryTokens.addAll(t);
            }
        }
        return queryTokens;
    }

    private ArrayList<Token> getToken(String stream, IntegerWrapper index) {
        if (index.val > stream.length()) {
            return null;
        }
        char c = stream.charAt(index.val);
        StringBuilder word = new StringBuilder();
        char ch;
        // Handle the current encountered character
        switch(c) {
            case '&':
                index.val++;
                return new ArrayList<>(List.of(new Token(Token.Type.AND)));
            case '|':
                index.val++;
                return new ArrayList<>(List.of(new Token(Token.Type.OR)));
            case '(':
                index.val++;
                return new ArrayList<>(List.of(new Token(Token.Type.LEFTPAREN)));
            case ')':
                index.val++;
                return new ArrayList<>(List.of(new Token(Token.Type.RIGHTPAREN)));
            case '!':
                index.val++;
                return new ArrayList<>(List.of(new Token(Token.Type.NOT)));
            case ' ':
                index.val++;
                return null;
            case '"':
                StringBuilder phrases = new StringBuilder();
                // Build the phrase string while not encountering close quotations or end of stream
                while (index.val < stream.length() - 1) {
                    index.val++;
                    ch = stream.charAt(index.val);
                    if (ch == '"') {
                        index.val++;
                        break;
                    } else if (Character.isWhitespace(ch)) {
                        // Append new word upon encountering whitespace
                        if (word.length() > 0) {
                            if (phrases.length() > 0) {
                                phrases.append(" ");
                            }
                            phrases.append(word);
                            word = new StringBuilder();
                        }
                    } else {
                        word.append(ch);
                    }
                }
                if (phrases.length() > 0) {
                    phrases.append(" ");
                }
                phrases.append(word);
                return new ArrayList<>(List.of(new Token(phrases.toString())));
            default:
                // Single word detected, read until an operator, parentheses, or whitespace
                while (index.val < stream.length()) {
                    ch = stream.charAt(index.val);
                    if (Character.isWhitespace(ch) || parens.contains(ch) || ops.contains(ch)) {
                        break;
                    }
                    word.append(ch);
                    index.val++;
                }
                return new ArrayList<>(List.of(new Token(word.toString().trim())));
        }
    }

    private QueryNode buildTree() {
        // Parse left side of a possible implicit AND
        QueryNode left = parseSubQuery();
        Token next = peekNextToken();
        // Implicit AND detected after left query
        while (next != null && queryStart.contains(next.type)) {
            QueryNode right = parseSubQuery();
            right = right == null ? new QueryNode(next) : right;
            // Construct and join implicit AND queries with an AND node
            QueryNode newNode = new QueryNode(new Token(Token.Type.AND));
            newNode.left = left;
            newNode.right = right;
            left = newNode;
            next = peekNextToken();
        }
        return left;
    }

    private QueryNode parseSubQuery() {
        Token t = getNextToken();
        if (t == null) {
            return null;
        }
        if (t.type == Token.Type.WORD) {
            // Single word base case
            return new QueryNode(t);
        } else if (t.type == Token.Type.LEFTPAREN) {
            // Recursively handle query within parentheses
            QueryNode left = buildTree();
            Token operator = getNextToken();
            QueryNode right = buildTree();
            getNextToken();
            if (operator == null || !(operator.type == Token.Type.AND || operator.type == Token.Type.OR)) {
                System.err.println("[Error] Query syntax error");
                return null;
            }
            // Use operator to join the left and right subqueries
            QueryNode opNode = new QueryNode(operator);
            opNode.left = left;
            opNode.right = right;
            return opNode;
        } else if (t.type == Token.Type.NOT) {
            // Instantiate a NOT node with its associated word as its only child
            QueryNode opNode = new QueryNode(t);
            Token word = getNextToken();
            if (word == null) {
                System.err.println("[Error] Query syntax error");
                return null;
            }
            opNode.left = new QueryNode(word);
            return opNode;
        } else {
            System.err.println("[Error] Query syntax error");
            return null;
        }
    }

    private Token getNextToken() {
        return !tokenStack.isEmpty() ? tokenStack.pop() : null;
    }

    private Token peekNextToken() {
        return !tokenStack.isEmpty() ? tokenStack.peek() : null;
    }

    public QueryNode getRoot() {
        return root;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        return preorder(sb, root, 0);
    }

    public String preorder(StringBuilder sb, QueryNode tree, int depth) {
        if (tree == null) return "";
        for (int i = 0; i < depth; i++) {
            sb.append("\t");
        }
        depth++;
        sb.append(tree.token).append("\n");
        preorder(sb, tree.left, depth);
        preorder(sb, tree.right, depth);
        return sb.toString();
    }

}
