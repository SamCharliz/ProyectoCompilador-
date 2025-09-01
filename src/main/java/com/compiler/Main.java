package com.compiler;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;
import com.compiler.lexer.NfaSimulator;

/**
 * Main class for demonstrating regex to NFA conversion and simulation.
 * This class builds an NFA from a regular expression and tests input strings.
 * (Práctica 2 - Parte 1: Solo conversión Regex → NFA)
 */
public class Main {
    /**
     * Default constructor for Main.
     */
    public Main() {}

    /**
     * Entry point for the NFA demo.
     * Steps:
     * 1. Parse regex to NFA
     * 2. Simulate NFA with test strings
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        // --- CONFIGURATION ---
        String regex = "a(b|c)*";
        String[] testStrings = {"a", "ab", "ac", "abbc", "acb", "", "b", "abcabc"};

        System.out.println("=== PRÁCTICA 2 - PARTE 1 ===");
        System.out.println("=== CONVERSIÓN REGEX → NFA ===\n");
        System.out.println("Testing Regex: " + regex);

        try {
            // --- STEP 1: Regex -> NFA---
            RegexParser parser = new RegexParser();
            NFA nfa = parser.parse(regex);
            
            System.out.println("\n--- NFA CREADO EXITOSAMENTE ---");
            System.out.println("NFA Start State: q" + nfa.getStartState().getId());
            System.out.println("NFA End State: q" + nfa.getEndState().getId() + 
                             " (Final: " + nfa.isEndStateAccepting() + ")");

            // --- STEP 2: NFA Simulation (para demostrar que funciona) ---
            NfaSimulator nfaSimulator = new NfaSimulator();
            System.out.println("\n--- SIMULACIÓN NFA ---");
            
            for (String s : testStrings) {
                boolean accepted = nfaSimulator.simulate(nfa, s);
                System.out.println("String '" + s + "': " + (accepted ? "Accepted" : "Rejected"));
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}