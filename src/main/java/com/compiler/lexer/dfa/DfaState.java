package com.compiler.lexer.dfa;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Objects;

import com.compiler.lexer.nfa.State;

/**
 * DfaState
 * --------
 * Represents a single state in a Deterministic Finite Automaton (DFA).
 * Each DFA state corresponds to a set of states from the original NFA.
 * Provides methods for managing transitions, checking finality, and equality based on NFA state sets.
 */
public class DfaState {
    private static int nextId = 0;
    
    /**
     * Unique identifier for this DFA state.
     */
    public final int id;
    
    /**
     * The set of NFA states this DFA state represents.
     */
    public final Set<State> nfaStates;
    
    /**
     * Indicates whether this DFA state is a final (accepting) state.
     */
    private boolean isFinal;
    
    /**
     * Map of input symbols to destination DFA states (transitions).
     */
    private final Map<Character, DfaState> transitions;

    /**
     * Constructs a new DFA state.
     * @param nfaStates The set of NFA states that this DFA state represents.
     */
    public DfaState(Set<State> nfaStates) {
        this.id = nextId++;
        this.nfaStates = nfaStates;
        this.transitions = new HashMap<>();
        this.isFinal = false;
        
        // Check if this DFA state should be final (if any of the NFA states is final)
        for (State nfaState : nfaStates) {
            if (nfaState.isFinal()) {
                this.isFinal = true;
                break;
            }
        }
    }

    /**
     * Returns all transitions from this state.
     * @return Map of input symbols to destination DFA states.
     */
    public Map<Character, DfaState> getTransitions() {
        return new HashMap<>(transitions); // Return a copy to preserve encapsulation
    }

    /**
     * Adds a transition from this state to another on a given symbol.
     * @param symbol The input symbol for the transition.
     * @param toState The destination DFA state.
     */
    public void addTransition(Character symbol, DfaState toState) {
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null for DFA transitions");
        }
        transitions.put(symbol, toState);
    }

    /**
     * Two DfaStates are considered equal if they represent the same set of NFA states.
     * @param obj The object to compare.
     * @return True if the states are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DfaState dfaState = (DfaState) obj;
        return Objects.equals(nfaStates, dfaState.nfaStates);
    }

    /**
     * The hash code is based on the set of NFA states.
     * @return The hash code for this DFA state.
     */
    @Override
    public int hashCode() {
        return Objects.hash(nfaStates);
    }
    
    /**
     * Returns a string representation of the DFA state, including its id and finality.
     * @return String representation of the state.
     */
    @Override
    public String toString() {
        return "DFAState{" +
               "id=" + id +
               ", nfaStates=" + nfaStates +
               ", isFinal=" + isFinal +
               '}';
    }

    /**
     * Sets the finality of the DFA state.
     * @param isFinal True if this state is a final state, false otherwise.
     */
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    /**
     * Checks if the DFA state is final.
     * @return True if this state is a final state, false otherwise.
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Gets the transition for a given input symbol.
     * @param symbol The input symbol for the transition.
     * @return The destination DFA state for the transition, or null if there is no transition for the given symbol.
     */
    public DfaState getTransition(char symbol) {
        return transitions.get(symbol);
    }

    /**
     * Returns the set of NFA states this DFA state represents.
     * @return The set of NFA states.
     */
    public Set<State> getName() {
        return nfaStates;
    }

    /**
     * Gets the unique identifier of this DFA state.
     * @return The state's ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Checks if this state has a transition for the given symbol.
     * @param symbol The input symbol to check.
     * @return True if a transition exists, false otherwise.
     */
    public boolean hasTransition(char symbol) {
        return transitions.containsKey(symbol);
    }

    /**
     * Returns the number of transitions from this state.
     * @return The number of transitions.
     */
    public int getTransitionCount() {
        return transitions.size();
    }
}