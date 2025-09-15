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

public class Main {

    public static void main(String[] args) {

        // --- DEFINIR ALFABETO ---
        Set<Character> alphabet = Set.of('a','b','c','d');

        // --- DEFINIR REGLAS DE TOKENS ---
        List<TokenRule> rules = new ArrayList<>();
        rules.add(new TokenRule("PLUS_A", "a+"));
        rules.add(new TokenRule("AB_STAR_C", "ab*c"));
        rules.add(new TokenRule("A_B_STAR", "(a|b)*"));
        rules.add(new TokenRule("AB_OR_CD", "a(b|c)d"));

        // --- CREAR TOKENIZER ---
        Tokenizer tokenizer = new Tokenizer(rules, alphabet);

        // --- CADENAS DE PRUEBA ---
        String[] testStrings = {"a", "aa", "ab", "abc", "abcd", "ac", "abd", "ad", "b", ""};

        // --- TOKENIZACIÓN ---
        System.out.println("=== TOKENIZACIÓN ===");
        for(String input : testStrings) {
            try {
                List<Token> tokens = tokenizer.tokenize(input);
                System.out.println("Input: '" + input + "' -> Tokens: " + tokens);
            } catch(Exception e) {
                System.out.println("Input: '" + input + "' -> ERROR: " + e.getMessage());
            }
        }

        // --- EJEMPLO: DFA Y MINIMIZACIÓN ---
        String regex = "a(b|c)*";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.getEndState().setFinal(true);

        DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
        System.out.println("\n--- DFA ORIGINAL ---");
        visualizeDfa(dfa);

        DFA minimizedDfa = DfaMinimizer.minimizeDfa(dfa, alphabet);
        System.out.println("\n--- DFA MINIMIZADO ---");
        visualizeDfa(minimizedDfa);

        // --- SIMULACIÓN ---
        DfaSimulator simulator = new DfaSimulator();
        System.out.println("\n--- SIMULACIÓN CON DFA MINIMIZADO ---");
        for(String s : testStrings) {
            boolean accepted = simulator.simulate(minimizedDfa, s);
            System.out.println("String '" + s + "': " + (accepted ? "Accepted" : "Rejected"));
        }
    }

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
