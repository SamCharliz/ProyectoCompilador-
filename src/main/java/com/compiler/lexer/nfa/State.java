package com.compiler.lexer.nfa;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents a state in a Non-deterministic Finite Automaton (NFA).
 * Each state has a unique identifier, a list of transitions to other states,
 * and a flag indicating whether it is a final (accepting) state.
 *
 * <p>
 * Fields:
 * <ul>
 *   <li>{@code id} - Unique identifier for the state.</li>
 *   <li>{@code transitions} - List of transitions from this state to others.</li>
 *   <li>{@code isFinal} - Indicates if this state is an accepting state.</li>
 * </ul>
 *
 * <p>
 * The {@code nextId} static field is used to assign unique IDs to each state.
 * </p>
 */
public class State {
    private static int nextId = 0;
    
    /**
     * Unique identifier for this state.
     */
    public final int id;

    /**
     * List of transitions from this state to other states.
     */
    public List<Transition> transitions;

    /**
     * Indicates if this state is a final (accepting) state.
     */
    public boolean isFinal;

    /**
     * Constructs a new state with a unique identifier and no transitions.
     * The state is not final by default.
     */
    public State() {
        this.id = nextId++;
        this.transitions = new ArrayList<>();
        this.isFinal = false;
    }

    /**
     * Checks if this state is a final (accepting) state.
     * @return true if this state is final, false otherwise
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Sets whether this state is a final (accepting) state.
     * @param isFinal true to set as final state, false otherwise
     */
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    /**
     * Sets whether this state is accepting.
     * @param isAccepting true to set as accepting state, false otherwise
     */
    public void setAccepting(boolean isAccepting) {
        this.isFinal = isAccepting;
    }

    /**
     * Checks if this state is an accepting state.
     * @return true if this state is accepting, false otherwise
     */
    public boolean isAccepting() {
        return isFinal;
    }

    /**
     * Returns the states reachable from this state via epsilon transitions (symbol == null).
     * @return a list of states reachable by epsilon transitions
     */
    public List<State> getEpsilonTransitions() {
        List<State> epsilonStates = new ArrayList<>();
        for (Transition transition : transitions) {
            if (transition.symbol == null) {
                epsilonStates.add(transition.to);
            }
        }
        return Collections.unmodifiableList(epsilonStates);
    }

    /**
     * Returns the states reachable from this state via a transition with the given symbol.
     * @param symbol the symbol for the transition
     * @return a list of states reachable by the given symbol
     */
    public List<State> getTransitions(char symbol) {
        List<State> symbolStates = new ArrayList<>();
        for (Transition transition : transitions) {
            if (transition.symbol != null && transition.symbol == symbol) {
                symbolStates.add(transition.to);
            }
        }
        return Collections.unmodifiableList(symbolStates);
    }

    /**
     * Adds a transition from this state to another state with the given symbol.
     * @param symbol the symbol for the transition (null for epsilon transitions)
     * @param toState the destination state
     */
    public void addTransition(Character symbol, State toState) {
        if (toState == null) {
            throw new IllegalArgumentException("Destination state cannot be null");
        }
        transitions.add(new Transition(symbol, toState));
    }

    /**
     * Adds an epsilon transition from this state to another state.
     * @param toState the destination state
     */
    public void addEpsilonTransition(State toState) {
        addTransition(null, toState);
    }

    /**
     * Returns all transitions from this state.
     * @return an unmodifiable list of transitions
     */
    public List<Transition> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }

    /**
     * Returns the unique identifier of this state.
     * @return the state ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns a string representation of the state.
     * @return string containing state ID and final status
     */
    @Override
    public String toString() {
        return "State{" +
               "id=" + id +
               ", isFinal=" + isFinal +
               ", transitions=" + transitions.size() +
               '}';
    }

    /**
     * Resets the static ID counter (mainly for testing purposes).
     */
    public static void resetIdCounter() {
        nextId = 0;
    }

    /**
     * Returns the number of transitions from this state.
     * @return the transition count
     */
    public int getTransitionCount() {
        return transitions.size();
    }

    /**
     * Checks if this state has any epsilon transitions.
     * @return true if there are epsilon transitions, false otherwise
     */
    public boolean hasEpsilonTransitions() {
        for (Transition transition : transitions) {
            if (transition.symbol == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this state has any transitions with the given symbol.
     * @param symbol the symbol to check
     * @return true if there are transitions with the symbol, false otherwise
     */
    public boolean hasTransitions(char symbol) {
        for (Transition transition : transitions) {
            if (transition.symbol != null && transition.symbol == symbol) {
                return true;
            }
        }
        return false;
    }
}