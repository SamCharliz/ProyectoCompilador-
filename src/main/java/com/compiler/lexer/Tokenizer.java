package com.compiler.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;

/**
 * Tokenizer performs lexical analysis by converting an input string into a list of tokens
 * according to a set of token rules. Each rule is defined by a regular expression and a token type.
 * 
 * The class precomputes a minimized DFA for each regex to efficiently match tokens using
 * the maximal munch (longest prefix) strategy.
 */
public class Tokenizer {

    /** List of token rules defining regex patterns and token types */
    private final List<TokenRule> rules;

    /** List of DFAs corresponding to each token rule */
    private final List<DFA> dfas;

    /**
     * Constructs a Tokenizer given token rules and an input alphabet.
     * Precomputes minimized DFAs for each regex.
     *
     * @param rules    The list of token rules to apply.
     * @param alphabet The set of characters used in the input language.
     */
    public Tokenizer(List<TokenRule> rules, Set<Character> alphabet) {
        this.rules = rules;
        this.dfas = new ArrayList<>();

        // Precompute DFA for each rule
        RegexParser parser = new RegexParser();
        for (TokenRule rule : rules) {
            try {
                NFA nfa = parser.parse(rule.getRegex());
                nfa.getEndState().setFinal(true);
                DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
                // Minimize DFA for better performance
                DFA minimizedDfa = DfaMinimizer.minimizeDfa(dfa, alphabet);
                dfas.add(minimizedDfa);
            } catch (Exception e) {
                System.err.println("Error parsing regex '" + rule.getRegex() + "': " + e.getMessage());
                // Add a null DFA to maintain index alignment
                dfas.add(null);
            }
        }
    }

    /**
     * Tokenizes the input string into a list of tokens using the precomputed DFAs.
     * Applies the maximal munch (longest match) rule for token recognition.
     * 
     * @param input The string to tokenize.
     * @return A list of tokens recognized from the input. Unmatched characters are returned
     *         as tokens with type "ERROR".
     */
    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int pos = 0;

        while (pos < input.length()) {
            int maxMatchLength = 0;
            TokenRule matchedRule = null;

            // Check each DFA for the longest matching prefix
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
                // No matching token: mark as error
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
     * Returns the length of the prefix accepted by the given DFA starting from startPos.
     * Implements the maximal munch (longest prefix) strategy.
     *
     * @param dfa      The DFA to use for matching.
     * @param input    The input string.
     * @param startPos The starting position in the input string.
     * @return The length of the longest prefix matched by the DFA.
     */
    private int matchDFA(DFA dfa, String input, int startPos) {
        int lastFinal = -1;
        DfaState current = dfa.startState;

        // Check if start state is final
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
