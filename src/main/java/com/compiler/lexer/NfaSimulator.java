package com.compiler.lexer;

import java.util.Set;
import java.util.HashSet;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

/**
 * NfaSimulator simulates the execution of a Nondeterministic Finite Automaton (NFA)
 * on a given input string. It supports epsilon transitions and checks
 * if the NFA accepts the input.
 */
public class NfaSimulator {
    
    /**
     * Default constructor for NfaSimulator.
     */
    public NfaSimulator() {
        // No initialization needed
    }

    /**
     * Simulates the given NFA on the input string.
     *
     * @param nfa   The NFA to simulate. Must not be null.
     * @param input The input string to test against the NFA. Null is treated as empty string.
     * @return True if the NFA accepts the input string, false otherwise.
     * @throws IllegalArgumentException If the NFA is null.
     */
    public boolean simulate(NFA nfa, String input) {
        if (nfa == null) {
            throw new IllegalArgumentException("NFA cannot be null");
        }
        if (input == null) {
            input = ""; // Treat null input as empty string
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

            // If no states remain, input is rejected
            if (currentStates.isEmpty()) {
                return false;
            }
        }

        // 3. Check if any of the current states is an accepting (final) state
        for (State state : currentStates) {
            if (state.isAccepting()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Computes the epsilon-closure of a state using a recursive approach.
     * The epsilon-closure is the set of states reachable from the given state
     * using only epsilon (empty string) transitions.
     *
     * @param start The state to compute the epsilon-closure for.
     * @return A set of states reachable from the start state via epsilon transitions.
     */
    private Set<State> epsilonClosure(State start) {
        Set<State> closure = new HashSet<>();
        addEpsilonClosureRecursive(start, closure);
        return closure;
    }

    /**
     * Helper recursive method to add all states reachable from the given state
     * via epsilon transitions into the closure set.
     *
     * @param state   The current state.
     * @param closure The set collecting all reachable states.
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
