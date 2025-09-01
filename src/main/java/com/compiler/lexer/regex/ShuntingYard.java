package com.compiler.lexer.regex;

import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for regular expression parsing using the Shunting Yard algorithm.
 * Converts infix regular expressions to postfix notation.
 */
public class ShuntingYard {
    private static final Map<Character, Integer> PRECEDENCE = new HashMap<>();
    
    static {
        PRECEDENCE.put('|', 1);  // Union - lowest precedence
        PRECEDENCE.put('.', 2);  // Concatenation - medium precedence
        PRECEDENCE.put('*', 3);  // Kleene star - highest precedence
        PRECEDENCE.put('?', 3);  // Optional - highest precedence
        PRECEDENCE.put('+', 3);  // Plus - highest precedence
    }

    /**
     * Default constructor for ShuntingYard.
     */
    public ShuntingYard() {
        // No initialization needed
    }

    /**
     * Inserts the explicit concatenation operator ('.') into the regular expression.
     */
    public static String insertConcatenationOperator(String regex) {
        if (regex == null || regex.isEmpty()) {
            return regex;
        }
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < regex.length(); i++) {
            char current = regex.charAt(i);
            result.append(current);
            
            if (i < regex.length() - 1) {
                char next = regex.charAt(i + 1);
                
                boolean needsConcatenation = false;
                
                // Conditions for implicit concatenation:
                if (isOperand(current) && (isOperand(next) || next == '(')) {
                    needsConcatenation = true;
                } else if (current == ')' && (isOperand(next) || next == '(')) {
                    needsConcatenation = true;
                } else if (current == '*' && (isOperand(next) || next == '(')) {
                    needsConcatenation = true;
                } else if (current == '?' && (isOperand(next) || next == '(')) {
                    needsConcatenation = true;
                } else if (current == '+' && (isOperand(next) || next == '(')) {
                    needsConcatenation = true;
                }
                
                if (needsConcatenation) {
                    result.append('.');
                }
            }
        }
        
        return result.toString();
    }

    /**
     * Determines if the given character is an operand.
     */
    private static boolean isOperand(char c) {
        return c != '|' && c != '*' && c != '.' && c != '?' && c != '+' && 
               c != '(' && c != ')';
    }

    /**
     * Determines if the given character is an operator.
     */
    private static boolean isOperator(char c) {
        return c == '|' || c == '*' || c == '.' || c == '?' || c == '+';
    }

    /**
     * Converts an infix regular expression to postfix notation.
     */
    public static String toPostfix(String infixRegex) {
        if (infixRegex == null || infixRegex.isEmpty()) {
            return infixRegex;
        }
        
        // First, insert explicit concatenation operators
        String withConcatenation = insertConcatenationOperator(infixRegex);
        System.out.println("With explicit concatenation: " + withConcatenation);
        
        StringBuilder output = new StringBuilder();
        Stack<Character> operatorStack = new Stack<>();
        
        for (int i = 0; i < withConcatenation.length(); i++) {
            char c = withConcatenation.charAt(i);
            
            if (isOperand(c)) {
                output.append(c);
            } else if (c == '(') {
                operatorStack.push(c);
            } else if (c == ')') {
                // Pop operators until '(' is found
                while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                    output.append(operatorStack.pop());
                }
                if (!operatorStack.isEmpty() && operatorStack.peek() == '(') {
                    operatorStack.pop(); // Remove '('
                } else {
                    throw new IllegalArgumentException("Mismatched parentheses");
                }
            } else if (isOperator(c)) {
                // Pop operators with higher or equal precedence
                while (!operatorStack.isEmpty() && 
                       operatorStack.peek() != '(' && 
                       PRECEDENCE.getOrDefault(operatorStack.peek(), 0) >= PRECEDENCE.getOrDefault(c, 0)) {
                    output.append(operatorStack.pop());
                }
                operatorStack.push(c);
            }
        }
        
        // Pop any remaining operators
        while (!operatorStack.isEmpty()) {
            char op = operatorStack.pop();
            if (op == '(') {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            output.append(op);
        }
        
        return output.toString();
    }
}