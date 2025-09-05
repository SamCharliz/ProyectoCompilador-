package com.compiler.lexer.dfa;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

// Importar la clase DfaMinimizer
import com.compiler.lexer.DfaMinimizer;

/**
 * DFA
 * ---
 * Represents a complete Deterministic Finite Automaton (DFA).
 * Contains the start state and a list of all states in the automaton.
 */
public class DFA {
    /**
     * The starting state of the DFA.
     */
    public final DfaState startState;

    /**
     * A list of all states in the DFA.
     */
    public final List<DfaState> allStates;
    
    /**
     * The alphabet of the DFA.
     */
    public final Set<Character> alphabet;

    /**
     * Constructs a new DFA.
     * @param startState The starting state of the DFA.
     * @param allStates  A list of all states in the DFA.
     * @param alphabet   The alphabet of the DFA.
     */
    public DFA(DfaState startState, List<DfaState> allStates, Set<Character> alphabet) {
        this.startState = startState;
        this.allStates = allStates;
        this.alphabet = alphabet;
    }

    /**
     * Checks if a string is accepted by this DFA.
     * @param input The input string to check.
     * @return true if the string is accepted, false otherwise.
     */
    public boolean accepts(String input) {
        DfaState currentState = startState;
        
        for (char c : input.toCharArray()) {
            currentState = currentState.getTransition(c);
            if (currentState == null) {
                return false; // No transition for this symbol
            }
        }
        
        return currentState.isFinal();
    }

    /**
     * Minimizes the DFA using the table-filling algorithm.
     * @return A minimized DFA.
     */
    public DFA minimize() {
        // Use the DfaMinimizer class to minimize this DFA
        return DfaMinimizer.minimizeDfa(this, this.alphabet);
    }

    /**
     * Returns a string representation of the DFA.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DFA:\n");
        sb.append("Alphabet: ").append(alphabet).append("\n");
        sb.append("Start State: ").append("D").append(startState.getId()).append("\n");
        sb.append("Final States: ");
        
        List<String> finalStateNames = new ArrayList<>();
        for (DfaState state : allStates) {
            if (state.isFinal()) {
                finalStateNames.add("D" + state.getId());
            }
        }
        sb.append(finalStateNames).append("\n");
        sb.append("States:\n");
        
        for (DfaState state : allStates) {
            sb.append("  D").append(state.getId());
            if (state.isFinal()) {
                sb.append(" (Final)");
            }
            sb.append(": ");
            
            // Show transitions
            boolean hasTransitions = false;
            for (char symbol : alphabet) {
                DfaState target = state.getTransition(symbol);
                if (target != null) {
                    sb.append(symbol).append("→D").append(target.getId()).append(" ");
                    hasTransitions = true;
                }
            }
            
            if (!hasTransitions) {
                sb.append("No transitions");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }

    /**
     * Finds a state by its ID.
     * @param id The ID of the state to find.
     * @return The DfaState with the given ID, or null if not found.
     */
    public DfaState findStateById(int id) {
        for (DfaState state : allStates) {
            if (state.getId() == id) {
                return state;
            }
        }
        return null;
    }

    /**
     * Gets all final states.
     * @return A list of all final states.
     */
    public List<DfaState> getFinalStates() {
        List<DfaState> finalStates = new ArrayList<>();
        for (DfaState state : allStates) {
            if (state.isFinal()) {
                finalStates.add(state);
            }
        }
        return finalStates;
    }

    /**
     * Checks if the DFA is complete (has transitions for all symbols in all states).
     * @return true if the DFA is complete, false otherwise.
     */
    public boolean isComplete() {
        for (DfaState state : allStates) {
            for (char symbol : alphabet) {
                if (state.getTransition(symbol) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Makes the DFA complete by adding a sink state if needed.
     * @return A complete DFA (may be the same instance if already complete).
     */
    public DFA makeComplete() {
        if (isComplete()) {
            return this;
        }
        
        // Create a sink state
        DfaState sinkState = new DfaState(new HashSet<>());
        sinkState.setFinal(false);
        
        List<DfaState> newAllStates = new ArrayList<>(allStates);
        newAllStates.add(sinkState);
        
        // Add missing transitions to sink state
        for (DfaState state : allStates) {
            for (char symbol : alphabet) {
                if (state.getTransition(symbol) == null) {
                    state.addTransition(symbol, sinkState);
                }
            }
        }
        
        // Add self-loops for sink state
        for (char symbol : alphabet) {
            sinkState.addTransition(symbol, sinkState);
        }
        
        return new DFA(startState, newAllStates, alphabet);
    }
}