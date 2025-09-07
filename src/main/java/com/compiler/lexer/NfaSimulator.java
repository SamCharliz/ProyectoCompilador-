package com.compiler.lexer;

import java.util.Set;
import java.util.HashSet;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

public class NfaSimulator {
    
    public NfaSimulator() {
        // No initialization needed
    }

    public boolean simulate(NFA nfa, String input) {
        if (nfa == null) {
            throw new IllegalArgumentException("NFA cannot be null");
        }
        if (input == null) {
            input = ""; // Handle null input as empty string
        }

        // 1. Initialize currentStates with epsilon-closure of NFA start state
        Set<State> currentStates = epsilonClosure(nfa.getStartState());

        // 2. Process each character in the input
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            Set<State> nextStates = new HashSet<>();

            for (State state : currentStates) {
                for (State nextState : state.getTransitions(c)) {
                    nextStates.addAll(epsilonClosure(nextState));
                }
            }

            currentStates = nextStates;
            
            if (currentStates.isEmpty()) {
                return false;
            }
        }

        // 3. Check if any final state is in currentStates
        for (State state : currentStates) {
            if (state.isAccepting()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Ahora epsilonClosure usa la versión recursiva
     */
    private Set<State> epsilonClosure(State start) {
        Set<State> closure = new HashSet<>();
        addEpsilonClosureRecursive(start, closure);
        return closure;
    }

    /**
     * Implementación recursiva de epsilon-closure
     */
    private void addEpsilonClosureRecursive(State state, Set<State> closure) {
        if (closure.contains(state)) {
            return;
        }
        
        closure.add(state);
        
        for (State epsilonState : state.getEpsilonTransitions()) {
            addEpsilonClosureRecursive(epsilonState, closure);
        }
    }
}
