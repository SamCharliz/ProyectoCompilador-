package com.compiler.lexer.regex;

import com.compiler.lexer.nfa.NFA;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

/**
 * RegexParser is responsible for parsing a regular expression string and converting it into
 * a corresponding Nondeterministic Finite Automaton (NFA). It supports operators such as
 * union (|), concatenation (.), Kleene star (*), plus (+), and optional (?).
 */
public class RegexParser {

    /** Precedence mapping for regex operators */
    private static final Map<Character, Integer> PRECEDENCE = new HashMap<>();
    static {
        PRECEDENCE.put('|', 1);
        PRECEDENCE.put('.', 2); // explicit concatenation
        PRECEDENCE.put('*', 3);
        PRECEDENCE.put('+', 3);
        PRECEDENCE.put('?', 3);
    }

    /**
     * Parses the input regex string and returns the equivalent NFA.
     *
     * @param regex The regular expression to parse.
     * @return NFA corresponding to the given regex.
     */
    public NFA parse(String regex) {
        String postfix = toPostfix(regex);
        return buildNFA(postfix);
    }

    /**
     * Converts an infix regular expression to postfix notation using the Shunting Yard algorithm.
     *
     * @param regex The infix regular expression.
     * @return The postfix representation of the regex.
     * @throws IllegalArgumentException if parentheses are mismatched or escapes are invalid.
     */
    public static String toPostfix(String regex) {
        if (regex == null || regex.isEmpty()) return regex;

        String withConcat = insertConcat(regex);
        StringBuilder output = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (int i = 0; i < withConcat.length(); i++) {
            char c = withConcat.charAt(i);

            if (c == '\\') {
                // Escape character, append next literal
                if (i + 1 >= withConcat.length()) throw new IllegalArgumentException("Invalid escape");
                output.append(c).append(withConcat.charAt(i + 1));
                i++;
                continue;
            }

            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    output.append(stack.pop());
                }
                if (stack.isEmpty()) throw new IllegalArgumentException("Mismatched parentheses");
                stack.pop(); // remove '('
            } else if (isOperator(c)) {
                while (!stack.isEmpty() && stack.peek() != '(' &&
                       PRECEDENCE.get(stack.peek()) >= PRECEDENCE.get(c)) {
                    output.append(stack.pop());
                }
                stack.push(c);
            } else {
                // Literal operand
                output.append(c);
            }
        }

        while (!stack.isEmpty()) {
            char op = stack.pop();
            if (op == '(' || op == ')') throw new IllegalArgumentException("Mismatched parentheses");
            output.append(op);
        }

        return output.toString();
    }

    /**
     * Inserts explicit concatenation operators ('.') where applicable in the regex.
     *
     * @param regex The input regex string.
     * @return Regex with explicit concatenation operators.
     */
    private static String insertConcat(String regex) {
        StringBuilder sb = new StringBuilder();
        char prev = 0;
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            if (prev != 0) {
                if ((isOperand(prev) || prev == '*' || prev == '+' || prev == '?' || prev == ')' || prev == ']') &&
                    (isOperand(c) || c == '(' || c == '[' || c == '\\')) {
                    sb.append('.');
                }
            }
            sb.append(c);
            prev = c;
        }
        return sb.toString();
    }

    /** Checks if a character is a literal operand (not an operator or bracket). */
    private static boolean isOperand(char c) {
        return !isOperator(c) && c != '(' && c != ')' && c != '[' && c != ']';
    }

    /** Checks if a character is a recognized regex operator. */
    private static boolean isOperator(char c) {
        return c == '|' || c == '*' || c == '+' || c == '?' || c == '.';
    }

    /**
     * Builds an NFA from a postfix regular expression.
     *
     * @param postfix The postfix regex string.
     * @return NFA representing the postfix regex.
     * @throws IllegalArgumentException if the postfix expression is invalid.
     */
    public static NFA buildNFA(String postfix) {
        Stack<NFA> stack = new Stack<>();
        for (int i = 0; i < postfix.length(); i++) {
            char c = postfix.charAt(i);

            if (c == '\\') {
                if (i + 1 >= postfix.length()) throw new IllegalArgumentException("Invalid escape");
                stack.push(NFA.createForCharacter(postfix.charAt(i + 1)));
                i++;
            } else if (c == '*') {
                if (stack.isEmpty()) throw new IllegalArgumentException("Invalid postfix *");
                stack.push(NFA.kleeneStar(stack.pop()));
            } else if (c == '+') {
                if (stack.isEmpty()) throw new IllegalArgumentException("Invalid postfix +");
                stack.push(NFA.plus(stack.pop()));
            } else if (c == '?') {
                if (stack.isEmpty()) throw new IllegalArgumentException("Invalid postfix ?");
                stack.push(NFA.optional(stack.pop()));
            } else if (c == '.') {
                if (stack.size() < 2) throw new IllegalArgumentException("Invalid postfix .");
                NFA b = stack.pop();
                NFA a = stack.pop();
                stack.push(NFA.concatenate(a, b));
            } else if (c == '|') {
                if (stack.size() < 2) throw new IllegalArgumentException("Invalid postfix |");
                NFA b = stack.pop();
                NFA a = stack.pop();
                stack.push(NFA.union(a, b));
            } else {
                stack.push(NFA.createForCharacter(c));
            }
        }

        if (stack.size() != 1) throw new IllegalArgumentException("Invalid postfix expression");
        return stack.pop();
    }
}
