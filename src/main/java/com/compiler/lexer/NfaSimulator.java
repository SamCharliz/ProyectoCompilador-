package com.compiler.lexer;

import java.util.Set;
import java.util.HashSet;
import java.util.Stack;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

/**
 * NfaSimulator
 * ------------
 * This class provides functionality to simulate a Non-deterministic Finite Automaton (NFA)
 * on a given input string. It determines whether the input string is accepted by the NFA by processing
 * each character and tracking the set of possible states, including those reachable via epsilon (ε) transitions.
 *
 * Simulation steps:
 * - Initialize the set of current states with the ε-closure of the NFA's start state.
 * - For each character in the input, compute the next set of states by following transitions labeled with that character,
 *   and include all states reachable via ε-transitions from those states.
 * - After processing the input, check if any of the current states is a final (accepting) state.
 *
 * The class also provides a helper method to compute the ε-closure of a given state, which is the set of all states
 * reachable from the given state using only ε-transitions.
 */
public class NfaSimulator {
    
    /**
     * Default constructor for NfaSimulator.
     */
    public NfaSimulator() {
        // No initialization needed
    }

    /**
     * Simulates the NFA on the given input string.
     * Starts at the NFA's start state and processes each character, following transitions and epsilon closures.
     * If any final state is reached after processing the input, the string is accepted.
     *
     * @param nfa The NFA to simulate.
     * @param input The input string to test.
     * @return True if the input is accepted by the NFA, false otherwise.
     */
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

            // For each state in currentStates
            for (State state : currentStates) {
                // Get all transitions for the current character
                for (State nextState : state.getTransitions(c)) {
                    // Add epsilon-closure of each reachable state
                    nextStates.addAll(epsilonClosure(nextState));
                }
            }

            currentStates = nextStates;
            
            // If no states remain, the input is rejected
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
     * Computes the epsilon-closure of a state: all states reachable via epsilon transitions.
     * Uses iterative DFS to avoid stack overflow with large NFAs.
     *
     * @param start The starting state.
     * @return Set of all states in the epsilon-closure.
     */
    private Set<State> epsilonClosure(State start) {
        Set<State> closure = new HashSet<>();
        Stack<State> stack = new Stack<>();
        
        stack.push(start);
        closure.add(start);
        
        while (!stack.isEmpty()) {
            State current = stack.pop();
            
            // Get all epsilon transitions from current state
            for (State epsilonState : current.getEpsilonTransitions()) {
                if (!closure.contains(epsilonState)) {
                    closure.add(epsilonState);
                    stack.push(epsilonState);
                }
            }
        }
        
        return closure;
    }

    /**
     * Alternative recursive implementation of epsilon-closure (for reference)
     * Note: May cause stack overflow with large NFAs.
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