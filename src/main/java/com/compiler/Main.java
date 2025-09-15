package com.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.compiler.lexer.Token;
import com.compiler.lexer.TokenRule;
import com.compiler.lexer.Tokenizer;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;
import com.compiler.lexer.DfaMinimizer;
import com.compiler.lexer.DfaSimulator;
import com.compiler.lexer.NfaToDfaConverter;

/**
 * Main class demonstrating the usage of the lexer components.
 * 
 * <p>This class shows:
 * <ul>
 *   <li>Definition of an input alphabet</li>
 *   <li>Creation of token rules with regular expressions</li>
 *   <li>Tokenization of example input strings</li>
 *   <li>Conversion of a regex to NFA and then DFA</li>
 *   <li>DFA minimization</li>
 *   <li>Simulation of DFA on input strings</li>
 *   <li>Visualization of DFA states and transitions</li>
 * </ul>
 */
public class Main {

    /**
     * Entry point of the program.
     *
     * @param args Command line arguments (unused)
     */
    public static void main(String[] args) {

        // --- DEFINE ALPHABET ---
        Set<Character> alphabet = Set.of('a','b','c','d');

        // --- DEFINE TOKEN RULES ---
        List<TokenRule> rules = new ArrayList<>();
        rules.add(new TokenRule("PLUS_A", "a+"));
        rules.add(new TokenRule("AB_STAR_C", "ab*c"));
        rules.add(new TokenRule("A_B_STAR", "(a|b)*"));
        rules.add(new TokenRule("AB_OR_CD", "a(b|c)d"));

        // --- CREATE TOKENIZER ---
        Tokenizer tokenizer = new Tokenizer(rules, alphabet);

        // --- TEST STRINGS ---
        String[] testStrings = {"a", "aa", "ab", "abc", "abcd", "ac", "abd", "ad", "b", ""};

        // --- TOKENIZATION EXAMPLES ---
        System.out.println("=== TOKENIZATION ===");
        for(String input : testStrings) {
            try {
                List<Token> tokens = tokenizer.tokenize(input);
                System.out.println("Input: '" + input + "' -> Tokens: " + tokens);
            } catch(Exception e) {
                System.out.println("Input: '" + input + "' -> ERROR: " + e.getMessage());
            }
        }

        // --- EXAMPLE: DFA CREATION AND MINIMIZATION ---
        String regex = "a(b|c)*";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.getEndState().setFinal(true);

        DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
        System.out.println("\n--- ORIGINAL DFA ---");
        visualizeDfa(dfa);

        DFA minimizedDfa = DfaMinimizer.minimizeDfa(dfa, alphabet);
        System.out.println("\n--- MINIMIZED DFA ---");
        visualizeDfa(minimizedDfa);

        // --- DFA SIMULATION ---
        DfaSimulator simulator = new DfaSimulator();
        System.out.println("\n--- SIMULATION WITH MINIMIZED DFA ---");
        for(String s : testStrings) {
            boolean accepted = simulator.simulate(minimizedDfa, s);
            System.out.println("String '" + s + "': " + (accepted ? "Accepted" : "Rejected"));
        }
    }

    /**
     * Prints the DFA's states, final states, and transitions in a readable format.
     *
     * @param dfa The DFA to visualize
     */
    public static void visualizeDfa(DFA dfa) {
        System.out.println("Start State: D" + dfa.startState.getId());
        for(DfaState state : dfa.allStates) {
            StringBuilder sb = new StringBuilder();
            sb.append("State D").append(state.getId());
            if(state.isFinal()) sb.append(" (Final)");
            sb.append(":");
            for(char symbol : dfa.alphabet) {
                DfaState target = state.getTransition(symbol);
                if(target != null) {
                    sb.append("\n  --'").append(symbol).append("'--> D").append(target.getId());
                }
            }
            System.out.println(sb.toString());
        }
        System.out.println("------------------------");
    }
}
