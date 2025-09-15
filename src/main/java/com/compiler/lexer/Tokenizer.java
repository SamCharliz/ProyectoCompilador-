package com.compiler.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;

public class Tokenizer {

    private final List<TokenRule> rules;
    private final List<DFA> dfas;

    public Tokenizer(List<TokenRule> rules, Set<Character> alphabet) {
        this.rules = rules;
        this.dfas = new ArrayList<>();

        // Precomputar DFA para cada regla
        RegexParser parser = new RegexParser();
        for (TokenRule rule : rules) {
            try {
                NFA nfa = parser.parse(rule.getRegex());
                nfa.getEndState().setFinal(true);
                DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
                // Minimizar el DFA para mejor rendimiento
                DFA minimizedDfa = DfaMinimizer.minimizeDfa(dfa, alphabet);
                dfas.add(minimizedDfa);
            } catch (Exception e) {
                System.err.println("Error parsing regex '" + rule.getRegex() + "': " + e.getMessage());
                // Add a null DFA to maintain index alignment
                dfas.add(null);
            }
        }
    }

    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int pos = 0;

        while (pos < input.length()) {
            int maxMatchLength = 0;
            TokenRule matchedRule = null;

            for (int i = 0; i < rules.size(); i++) {
                DFA dfa = dfas.get(i);
                if (dfa == null) continue; // Skip invalid DFAs
                
                int length = matchDFA(dfa, input, pos);

                if (length > maxMatchLength) {
                    maxMatchLength = length;
                    matchedRule = rules.get(i);
                }
            }

            if (maxMatchLength == 0) {
                // No coincide ningún token: marcar como error
                tokens.add(new Token("ERROR", String.valueOf(input.charAt(pos))));
                pos++;
            } else {
                String lexeme = input.substring(pos, pos + maxMatchLength);
                tokens.add(new Token(matchedRule.getTokenType(), lexeme));
                pos += maxMatchLength;
            }
        }

        return tokens;
    }

    /** 
     * Devuelve la longitud del prefijo aceptado por el DFA (maximal munch)
     */
    private int matchDFA(DFA dfa, String input, int startPos) {
        int lastFinal = -1;
        DfaState current = dfa.startState;

        // Verificar si el estado inicial es final
        if (current.isFinal()) {
            lastFinal = startPos;
        }

        for (int i = startPos; i < input.length(); i++) {
            char c = input.charAt(i);
            current = current.getTransition(c);
            
            if (current == null) {
                break;
            }
            
            if (current.isFinal()) {
                lastFinal = i + 1;
            }
        }

        return lastFinal >= startPos ? lastFinal - startPos : 0;
    }
}