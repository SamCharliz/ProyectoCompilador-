package com.compiler.lexer.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * ShuntingYard provides utility methods to convert infix regular expressions to postfix
 * notation (Reverse Polish Notation) using the Shunting Yard algorithm.
 * It supports standard regex operators (union, concatenation, Kleene star, plus, optional),
 * escaped characters, and character classes.
 */
public class ShuntingYard {

    /** Operator precedence mapping for regex operators */
    private static final Map<String, Integer> PRECEDENCE = new HashMap<>();

    static {
        PRECEDENCE.put("|", 1); // union
        PRECEDENCE.put(".", 2); // explicit concatenation
        PRECEDENCE.put("*", 3); // Kleene star
        PRECEDENCE.put("?", 3); // optional
        PRECEDENCE.put("+", 3); // one or more
    }

    /**
     * Converts an infix regular expression to postfix notation.
     * Supports escaped characters and character classes.
     *
     * @param regex The infix regular expression string.
     * @return The equivalent postfix (RPN) expression.
     * @throws IllegalArgumentException If parentheses are mismatched or token is unknown.
     */
    public static String toPostfix(String regex) {
        if (regex == null || regex.isEmpty()) return regex;

        String withConcat = insertConcatenationOperator(regex);

        StringBuilder output = new StringBuilder();
        Stack<String> stack = new Stack<>();

        for (int i = 0; i < withConcat.length();) {
            String token = getToken(withConcat, i);
            i += token.length();

            if (isOperand(token)) {
                output.append(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.append(stack.pop());
                }
                if (stack.isEmpty()) throw new IllegalArgumentException("Mismatched parentheses");
                stack.pop(); // remove '('
            } else if (isOperator(token)) {
                while (!stack.isEmpty() && !stack.peek().equals("(") &&
                       PRECEDENCE.get(stack.peek()) >= PRECEDENCE.get(token)) {
                    output.append(stack.pop());
                }
                stack.push(token);
            } else {
                throw new IllegalArgumentException("Unknown token: " + token);
            }
        }

        while (!stack.isEmpty()) {
            String t = stack.pop();
            if (t.equals("(")) throw new IllegalArgumentException("Mismatched parentheses");
            output.append(t);
        }

        return output.toString();
    }

    /**
     * Inserts explicit concatenation operators ('.') where applicable in the regex.
     *
     * @param regex The original infix regex.
     * @return Regex with explicit concatenation operators inserted.
     */
    public static String insertConcatenationOperator(String regex) {
        if (regex == null || regex.isEmpty()) return regex;

        StringBuilder result = new StringBuilder();
        String prevToken = getToken(regex, 0);
        result.append(prevToken);

        for (int i = prevToken.length(); i < regex.length();) {
            String currentToken = getToken(regex, i);
            boolean needsConcat = (isOperand(prevToken) || prevToken.equals("*") || prevToken.equals("?") ||
                                   prevToken.equals("+") || prevToken.equals(")")) &&
                                  (isOperand(currentToken) || currentToken.equals("("));
            if (needsConcat) result.append('.');
            result.append(currentToken);

            i += currentToken.length();
            prevToken = currentToken;
        }

        return result.toString();
    }

    /**
     * Extracts a full token from the regex at the specified index.
     * Supports:
     *  - Escaped characters (e.g., \+, \*, \(, \))
     *  - Character classes (e.g., [a-z], [0-9A-Z])
     *
     * @param regex The regex string.
     * @param index The starting index of the token.
     * @return The complete token string.
     * @throws IllegalArgumentException If escape or character class is invalid.
     */
    public static String getToken(String regex, int index) {
        char c = regex.charAt(index);

        if (c == '\\') {
            if (index + 1 >= regex.length())
                throw new IllegalArgumentException("Dangling escape at end of regex");
            return "\\" + regex.charAt(index + 1);
        } else if (c == '[') {
            int end = regex.indexOf(']', index);
            if (end == -1)
                throw new IllegalArgumentException("Unterminated character class");
            return regex.substring(index, end + 1);
        } else {
            return String.valueOf(c);
        }
    }

    /** Checks if the token is a literal operand (not an operator or parenthesis). */
    private static boolean isOperand(String token) {
        return !isOperator(token) && !token.equals("(") && !token.equals(")");
    }

    /** Checks if the token is a recognized regex operator. */
    private static boolean isOperator(String token) {
        return PRECEDENCE.containsKey(token);
    }
}
