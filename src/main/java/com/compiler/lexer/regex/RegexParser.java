package com.compiler.lexer.regex;

import java.util.Stack;

import com.compiler.lexer.nfa.NFA;

public class RegexParser {

    public RegexParser() {
        // No initialization needed
    }

    public NFA parse(String infixRegex) {
        if (infixRegex == null || infixRegex.isEmpty()) {
            throw new IllegalArgumentException("Regular expression cannot be null or empty");
        }
        
        // PRIMERO: Insertar concatenación explícita
        String withConcatenation = insertExplicitConcatenation(infixRegex);
        System.out.println("With concatenation: " + withConcatenation);
        
        // SEGUNDO: Convertir a postfijo usando ShuntingYard
        String postfix = ShuntingYard.toPostfix(withConcatenation);
        System.out.println("Postfix expression: " + postfix);
        
        // TERCERO: Construir NFA
        return buildNfaFromPostfix(postfix);
    }

    /**
     * Inserta operadores de concatenación explícitos donde sea necesario
     */
    private String insertExplicitConcatenation(String regex) {
        if (regex.isEmpty()) return regex;
        
        StringBuilder result = new StringBuilder();
        char prev = regex.charAt(0);
        result.append(prev);
        
        for (int i = 1; i < regex.length(); i++) {
            char current = regex.charAt(i);
            
            // Condiciones para insertar concatenación explícita
            boolean needsConcatenation = 
                (isOperand(prev) && (isOperand(current) || current == '(')) ||
                (prev == ')' && (isOperand(current) || current == '(')) ||
                (prev == '*' && (isOperand(current) || current == '(')) ||
                (prev == '?' && (isOperand(current) || current == '(')) ||
                (prev == '+' && (isOperand(current) || current == '('));
            
            if (needsConcatenation) {
                result.append('.');
            }
            
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
                    case '.':
                        handleConcatenation(nfaStack);
                        break;
                    case '|':
                        handleUnion(nfaStack);
                        break;
                    case '*':
                        handleKleeneStar(nfaStack);
                        break;
                    case '?':
                        handleOptional(nfaStack);
                        break;
                    case '+':
                        handlePlus(nfaStack);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown operator: " + c);
                }
            }
        }
        
        if (nfaStack.size() != 1) {
            throw new IllegalArgumentException("Invalid regular expression: multiple NFAs remaining");
        }
        
        return nfaStack.pop();
    }

    private void handleOptional(Stack<NFA> stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Not enough NFAs for optional operation");
        }
        NFA nfa = stack.pop();
        stack.push(NFA.optional(nfa));
    }

    private void handlePlus(Stack<NFA> stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Not enough NFAs for plus operation");
        }
        NFA nfa = stack.pop();
        stack.push(NFA.plus(nfa));
    }

    private void handleConcatenation(Stack<NFA> stack) {
        if (stack.size() < 2) {
            throw new IllegalArgumentException("Not enough NFAs for concatenation");
        }
        NFA right = stack.pop();
        NFA left = stack.pop();
        stack.push(NFA.concatenate(left, right));
    }

    private void handleUnion(Stack<NFA> stack) {
        if (stack.size() < 2) {
            throw new IllegalArgumentException("Not enough NFAs for union");
        }
        NFA right = stack.pop();
        NFA left = stack.pop();
        stack.push(NFA.union(left, right));
    }

    private void handleKleeneStar(Stack<NFA> stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Not enough NFAs for Kleene star");
        }
        NFA nfa = stack.pop();
        stack.push(NFA.kleeneStar(nfa));
    }

    private boolean isOperator(char c) {
        return c == '|' || c == '.' || c == '*' || c == '?' || c == '+';
    }

    private boolean isOperand(char c) {
        return !isOperator(c) && c != '(' && c != ')';
    }
}