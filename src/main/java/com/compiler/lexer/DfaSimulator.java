package com.compiler.lexer;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

/**
 * DfaSimulator
 * ---
 * Provides functionality to simulate a deterministic finite automaton (DFA) 
 * on a given input string.
 */
public class DfaSimulator {

    /**
     * Simulates the given DFA on the input string.
     *
     * @param dfa   The DFA to simulate.
     * @param input The input string to test.
     * @return true if the DFA accepts the input, false otherwise.
     */
    public boolean simulate(DFA dfa, String input) {
        if (dfa == null || input == null) {
            return false;
        }

        DfaState currentState = dfa.startState;

        for (char c : input.toCharArray()) {
            currentState = currentState.getTransition(c);
            if (currentState == null) {
                return false; // No transition for this symbol
            }
        }

        return currentState.isFinal();
    }
}
