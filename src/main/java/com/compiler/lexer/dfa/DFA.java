package com.compiler.lexer.dfa;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import com.compiler.lexer.DfaMinimizer;

/**
 * DFA
 * ---
 * Represents a Deterministic Finite Automaton (DFA) with a start state,
 * a set of all states, and an alphabet.
 */
public class DFA {

    /** The starting state of the DFA. */
    public final DfaState startState;

    /** A list of all states in the DFA. */
    public final List<DfaState> allStates;

    /** The alphabet of the DFA. */
    public final Set<Character> alphabet;

    /**
     * Constructs a new DFA.
     *
     * @param startState The starting state of the DFA.
     * @param allStates  A list of all DFA states.
     * @param alphabet   The alphabet of the DFA.
     */
    public DFA(DfaState startState, List<DfaState> allStates, Set<Character> alphabet) {
        this.startState = startState;
        this.allStates = allStates;
        this.alphabet = alphabet;
    }

    /**
     * Checks if the DFA accepts a given input string.
     *
     * @param input The input string to check.
     * @return true if the DFA accepts the input, false otherwise.
     */
    public boolean accepts(String input) {
        DfaState currentState = startState;
        for (char c : input.toCharArray()) {
            currentState = currentState.getTransition(c);
            if (currentState == null) return false;
        }
        return currentState.isFinal();
    }

    /**
     * Minimizes the DFA using a table-filling algorithm.
     *
     * @return A minimized DFA.
     */
    public DFA minimize() {
        return DfaMinimizer.minimizeDfa(this, this.alphabet);
    }

    /**
     * Returns a list of all states in the DFA.
     *
     * @return List of all DfaState objects.
     */
    public List<DfaState> getStates() {
        return allStates;
    }

    /**
     * Prints all transitions of the DFA in the format:
     * D0 -a-> D1
     * D1 -b-> D2
     */
    public void printTransitions() {
        for (DfaState state : allStates) {
            for (char symbol : alphabet) {
                DfaState target = state.getTransition(symbol);
                if (target != null) {
                    System.out.println("D" + state.getId() + " -" + symbol + "-> D" + target.getId());
                }
            }
        }
    }

    /**
     * Finds a state by its ID.
     *
     * @param id The state ID.
     * @return The state with the given ID, or null if not found.
     */
    public DfaState findStateById(int id) {
        for (DfaState state : allStates) {
            if (state.getId() == id) return state;
        }
        return null;
    }

    /**
     * Returns a list of all final states in the DFA.
     *
     * @return List of final states.
     */
    public List<DfaState> getFinalStates() {
        List<DfaState> finals = new ArrayList<>();
        for (DfaState state : allStates) {
            if (state.isFinal()) finals.add(state);
        }
        return finals;
    }

    /**
     * Checks if the DFA is complete (has transitions for all symbols in all states).
     *
     * @return true if complete, false otherwise.
     */
    public boolean isComplete() {
        for (DfaState state : allStates) {
            for (char symbol : alphabet) {
                if (state.getTransition(symbol) == null) return false;
            }
        }
        return true;
    }

    /**
     * Makes the DFA complete by adding a sink state if necessary.
     *
     * @return A complete DFA.
     */
    public DFA makeComplete() {
        if (isComplete()) return this;

        DfaState sink = new DfaState(new HashSet<>());
        sink.setFinal(false);

        List<DfaState> newStates = new ArrayList<>(allStates);
        newStates.add(sink);

        for (DfaState state : allStates) {
            for (char symbol : alphabet) {
                if (state.getTransition(symbol) == null) {
                    state.addTransition(symbol, sink);
                }
            }
        }

        for (char symbol : alphabet) {
            sink.addTransition(symbol, sink);
        }

        return new DFA(startState, newStates, alphabet);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DFA:\nAlphabet: ").append(alphabet).append("\nStart State: D").append(startState.getId()).append("\n");
        sb.append("Final States: ").append(getFinalStates().stream().map(s -> "D" + s.getId()).toList()).append("\nStates:\n");

        for (DfaState state : allStates) {
            sb.append("  D").append(state.getId());
            if (state.isFinal()) sb.append(" (Final)");
            sb.append(": ");
            boolean hasTransition = false;
            for (char symbol : alphabet) {
                DfaState target = state.getTransition(symbol);
                if (target != null) {
                    sb.append(symbol).append("→D").append(target.getId()).append(" ");
                    hasTransition = true;
                }
            }
            if (!hasTransition) sb.append("No transitions");
            sb.append("\n");
        }
        return sb.toString();
    }
}
