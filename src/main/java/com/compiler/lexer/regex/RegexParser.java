package com.compiler.lexer.regex;

import java.util.Stack;

import com.compiler.lexer.nfa.NFA;

public class RegexParser {

    public NFA parse(String infixRegex) {
        if (infixRegex == null || infixRegex.isEmpty()) {
            throw new IllegalArgumentException("Regular expression cannot be null or empty");
        }
        
        // 1. Insertar concatenación explícita
        String withConcatenation = insertExplicitConcatenation(infixRegex);
        System.out.println("With concatenation: " + withConcatenation);
        
        // 2. Convertir a postfijo
        String postfix = ShuntingYard.toPostfix(withConcatenation);
        System.out.println("Postfix expression: " + postfix);
        
        // 3. Construir NFA
        return buildNfaFromPostfix(postfix);
    }

    private String insertExplicitConcatenation(String regex) {
        if (regex.isEmpty()) return regex;
        
        StringBuilder result = new StringBuilder();
        char prev = regex.charAt(0);
        result.append(prev);
        
        for (int i = 1; i < regex.length(); i++) {
            char current = regex.charAt(i);
            boolean needsConcat = 
                (isOperand(prev) && (isOperand(current) || current == '(')) ||
                (prev == ')' && (isOperand(current) || current == '(')) ||
                (prev == '*' && (isOperand(current) || current == '(')) ||
                (prev == '?' && (isOperand(current) || current == '(')) ||
                (prev == '+' && (isOperand(current) || current == '('));
            
            if (needsConcat) result.append('.');
            result.append(current);
            prev = current;
        }
        return result.toString();
    }

    private NFA buildNfaFromPostfix(String postfixRegex) {
        Stack<NFA> nfaStack = new Stack<>();
        
        for (char c : postfixRegex.toCharArray()) {
            if (isOperand(c)) {
                nfaStack.push(NFA.createForCharacter(c));
            } else {
                switch (c) {
                    case '.': handleConcatenation(nfaStack); break;
                    case '|': handleUnion(nfaStack); break;
                    case '*': handleKleeneStar(nfaStack); break;
                    case '?': handleOptional(nfaStack); break;
                    case '+': handlePlus(nfaStack); break;
                    default:
                        throw new IllegalArgumentException("Unknown operator: " + c);
                }
            }
        }
        if (nfaStack.size() != 1) {
            throw new IllegalArgumentException("Invalid regex: multiple NFAs remaining");
        }
        return nfaStack.pop();
    }

    private void handleOptional(Stack<NFA> stack) {
        stack.push(NFA.optional(stack.pop()));
    }
    private void handlePlus(Stack<NFA> stack) {
        stack.push(NFA.plus(stack.pop()));
    }
    private void handleConcatenation(Stack<NFA> stack) {
        NFA right = stack.pop();
        NFA left = stack.pop();
        stack.push(NFA.concatenate(left, right));
    }
    private void handleUnion(Stack<NFA> stack) {
        NFA right = stack.pop();
        NFA left = stack.pop();
        stack.push(NFA.union(left, right));
    }
    private void handleKleeneStar(Stack<NFA> stack) {
        stack.push(NFA.kleeneStar(stack.pop()));
    }

    private boolean isOperator(char c) {
        return c == '|' || c == '.' || c == '*' || c == '?' || c == '+';
    }
    private boolean isOperand(char c) {
        return !isOperator(c) && c != '(' && c != ')';
    }
}
