package com.compiler.lexer.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ShuntingYard {
    private static final Map<String, Integer> PRECEDENCE = new HashMap<>();

    static {
        PRECEDENCE.put("|", 1);
        PRECEDENCE.put(".", 2); // concatenación explícita
        PRECEDENCE.put("*", 3);
        PRECEDENCE.put("?", 3);
        PRECEDENCE.put("+", 3);
    }

    /**
     * Convierte un regex infijo to postfix, soportando escapes y clases de caracteres.
     */
    public static String toPostfix(String regex) {
        if (regex == null || regex.isEmpty()) return regex;

        String withConcat = insertConcatenationOperator(regex);
        // Removed debug print: System.out.println("With explicit concatenation: " + withConcat);

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
                stack.pop(); // sacar '('
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
     * Inserta concatenaciones explícitas '.' en un regex.
     */
    public static String insertConcatenationOperator(String regex) {
        if (regex == null || regex.isEmpty()) return regex;

        StringBuilder result = new StringBuilder();
        String prevToken = getToken(regex, 0);
        result.append(prevToken);

        for (int i = prevToken.length(); i < regex.length();) {
            String currentToken = getToken(regex, i);
            boolean needsConcat = (isOperand(prevToken) || prevToken.equals("*") || prevToken.equals("?") || prevToken.equals("+") || prevToken.equals(")")) &&
                                  (isOperand(currentToken) || currentToken.equals("("));
            if (needsConcat) result.append('.');
            result.append(currentToken);

            i += currentToken.length();
            prevToken = currentToken;
        }

        return result.toString();
    }

    /**
     * Devuelve un token completo, soportando:
     *  - Escapes: \+, \*, \(, \)
     *  - Clases de caracteres: [a-z], [0-9A-Z]
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

    private static boolean isOperand(String token) {
        return !isOperator(token) && !token.equals("(") && !token.equals(")");
    }

    private static boolean isOperator(String token) {
        return PRECEDENCE.containsKey(token);
    }
}
