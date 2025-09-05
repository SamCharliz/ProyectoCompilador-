package com.compiler.lexer;

import java.util.*;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

/**
 * NfaToDfaConverter
 * -----------------
 * Converts a Non-deterministic Finite Automaton (NFA) into
 * a Deterministic Finite Automaton (DFA) using the subset construction algorithm.
 */
public class NfaToDfaConverter {

    /**
     * Converts an NFA to a DFA using subset construction.
     *
     * @param nfa      The input NFA
     * @param alphabet The input alphabet
     * @return The equivalent DFA
     */
    public static DFA convertNfaToDfa(NFA nfa, Set<Character> alphabet) {
        // Reset DFA IDs for reproducibility
        DfaState.resetIdCounter();

        // 1. Compute epsilon-closure of NFA start state
        Set<State> startNfaStates = epsilonClosure(Collections.singleton(nfa.getStartState()));
        DfaState startDfaState = new DfaState(startNfaStates);

        List<DfaState> allDfaStates = new ArrayList<>();
        allDfaStates.add(startDfaState);

        Queue<DfaState> unmarkedStates = new LinkedList<>();
        unmarkedStates.add(startDfaState);

        // 2. Process unmarked DFA states
        while (!unmarkedStates.isEmpty()) {
            DfaState currentDfaState = unmarkedStates.poll();

            for (char symbol : alphabet) {
                Set<State> moveResult = move(currentDfaState.getNfaStates(), symbol);
                if (moveResult.isEmpty()) continue;

                Set<State> closure = epsilonClosure(moveResult);

                DfaState existingState = findDfaState(allDfaStates, closure);
                if (existingState == null) {
                    DfaState newDfaState = new DfaState(closure);
                    allDfaStates.add(newDfaState);
                    unmarkedStates.add(newDfaState);
                    currentDfaState.addTransition(symbol, newDfaState);
                } else {
                    currentDfaState.addTransition(symbol, existingState);
                }
            }
        }

        return new DFA(startDfaState, allDfaStates, alphabet);
    }

    /**
     * Computes epsilon-closure of a set of NFA states.
     *
     * @param states Set of NFA states
     * @return Epsilon-closure set
     */
    private static Set<State> epsilonClosure(Set<State> states) {
        Set<State> closure = new HashSet<>(states);
        Queue<State> queue = new LinkedList<>(states);

        while (!queue.isEmpty()) {
            State state = queue.poll();
            for (State epsilonTarget : state.getEpsilonTransitions()) {
                if (!closure.contains(epsilonTarget)) {
                    closure.add(epsilonTarget);
                    queue.add(epsilonTarget);
                }
            }
        }

        return closure;
    }

    /**
     * Computes the set of NFA states reachable from given states on a symbol.
     *
     * @param states Set of NFA states
     * @param symbol Input symbol
     * @return Set of reachable NFA states
     */
    private static Set<State> move(Set<State> states, char symbol) {
        Set<State> result = new HashSet<>();
        for (State state : states) {
            result.addAll(state.getTransitions(symbol));
        }
        return result;
    }

    /**
     * Finds an existing DFA state representing the same set of NFA states.
     *
     * @param dfaStates List of DFA states
     * @param targetNfaStates Target NFA states set
     * @return Matching DFA state or null if not found
     */
    private static DfaState findDfaState(List<DfaState> dfaStates, Set<State> targetNfaStates) {
        for (DfaState dfaState : dfaStates) {
            if (dfaState.getNfaStates().equals(targetNfaStates)) {
                return dfaState;
            }
        }
        return null;
    }
}
