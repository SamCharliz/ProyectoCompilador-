package com.compiler.lexer.regex;

import com.compiler.lexer.nfa.NFA;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

public class RegexParser {
    private static final Map<Character, Integer> PRECEDENCE = new HashMap<>();
    static {
        PRECEDENCE.put('|', 1);
        PRECEDENCE.put('.', 2); // concatenación explícita
        PRECEDENCE.put('*', 3);
        PRECEDENCE.put('+', 3);
        PRECEDENCE.put('?', 3);
    }

    public NFA parse(String regex) {
        String postfix = toPostfix(regex);
        return buildNFA(postfix);
    }

    /** Convierte un regex a postfix usando Shunting Yard */
    public static String toPostfix(String regex) {
        if (regex == null || regex.isEmpty()) return regex;

        String withConcat = insertConcat(regex);
        StringBuilder output = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (int i = 0; i < withConcat.length(); i++) {
            char c = withConcat.charAt(i);

            if (c == '\\') {
                // Escape, añadir siguiente literal
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
                stack.pop(); // quitar '('
            } else if (isOperator(c)) {
                while (!stack.isEmpty() && stack.peek() != '(' &&
                       PRECEDENCE.get(stack.peek()) >= PRECEDENCE.get(c)) {
                    output.append(stack.pop());
                }
                stack.push(c);
            } else {
                // Operando literal
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

    /** Inserta concatenación explícita '.' donde aplica */
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

    private static boolean isOperand(char c) {
        return !isOperator(c) && c != '(' && c != ')' && c != '[' && c != ']';
    }

    private static boolean isOperator(char c) {
        return c == '|' || c == '*' || c == '+' || c == '?' || c == '.';
    }

    /** Construye un NFA a partir de postfix seguro */
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