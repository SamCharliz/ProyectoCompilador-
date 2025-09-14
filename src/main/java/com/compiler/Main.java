package com.compiler;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import com.compiler.lexer.Token;
import com.compiler.lexer.TokenRule;
import com.compiler.lexer.Tokenizer;
import com.compiler.lexer.DfaMinimizer;
import com.compiler.lexer.DfaSimulator;
import com.compiler.lexer.NfaToDfaConverter;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;

public class Main {
    public static void main(String[] args) {
        // --- CONFIGURACIÓN DEL ALFABETO ---
        Set<Character> alphabet = Set.of('a','b','c','d');

        // --- DEFINIR REGLAS DE TOKENS ---
        List<TokenRule> rules = new ArrayList<>();
        rules.add(new TokenRule("a+", "PLUS_A"));
        rules.add(new TokenRule("a?", "OPTIONAL_A"));
        rules.add(new TokenRule("a|b", "A_OR_B"));
        rules.add(new TokenRule("ab|c", "AB_OR_C"));
        rules.add(new TokenRule("ab*c", "AB_STAR_C"));
        rules.add(new TokenRule("(a|b)*", "A_B_STAR"));
        rules.add(new TokenRule("a(b|c)d", "AB_OR_CD"));
        rules.add(new TokenRule("a(b*|c+)?d", "COMPLEX"));
        rules.add(new TokenRule("(a*)*", "NESTED_STAR"));
        rules.add(new TokenRule("a(b?)c", "OPTIONAL_B_C"));
        rules.add(new TokenRule("(a|b)*a(a|b)*", "CONTAINS_A"));

        // --- CREAR TOKENIZER ---
        Tokenizer tokenizer = new Tokenizer(rules, alphabet);

        // --- TEST DE STRINGS ---
        String[] testStrings = {"a", "aa", "ab", "abc", "abcd", "ac", "abd", "ad", "b", ""};

        System.out.println("=== TOKENIZACIÓN ===");
        for(String s : testStrings) {
            try {
                List<Token> tokens = tokenizer.tokenize(s);
                System.out.println("Input: '" + s + "' -> Tokens: " + tokens);
            } catch(Exception e) {
                System.out.println("Input: '" + s + "' -> ERROR: " + e.getMessage());
            }
        }

        // --- EJEMPLO: DFA Y MINIMIZACIÓN ---
        String regex = "a(b|c)*";
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.getEndState().setAccepting(true);

        DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, Set.of('a','b','c'));
        System.out.println("\n--- DFA ORIGINAL ---");
        visualizeDfa(dfa);

        DFA minimizedDfa = DfaMinimizer.minimizeDfa(dfa, Set.of('a','b','c'));
        System.out.println("\n--- DFA MINIMIZADO ---");
        visualizeDfa(minimizedDfa);

        DfaSimulator simulator = new DfaSimulator();
        System.out.println("\n--- SIMULACIÓN MINIMIZADA ---");
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
        System.out.println("------------------------\n");
    }
}
