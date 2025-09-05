package com.compiler.lexer;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

public class DfaSimulator {
    
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