package com.compiler.lexer.nfa;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents a state in a Non-deterministic Finite Automaton (NFA).
 */
public class State {
    private static int nextId = 0;

    /** Unique identifier for this state */
    private final int id;

    /** List of transitions from this state to other states */
    private final List<Transition> transitions;

    /** Indicates if this state is a final (accepting) state */
    private boolean isFinal;

    /** Type of token recognized if this state is final (nullable) */
    private String tokenType;

    

    /** Constructs a new state with a unique identifier and no transitions. */
    public State() {
        this.id = nextId++;
        this.transitions = new ArrayList<>();
        this.isFinal = false;
        this.tokenType = null;
    }

    /** Returns the unique identifier of this state. */
    public int getId() { return id; }

    /** Returns whether this state is a final (accepting) state. */
    public boolean isFinal() { return isFinal; }

    /** Sets whether this state is a final (accepting) state. */
    public void setFinal(boolean isFinal) { this.isFinal = isFinal; }

    /** Returns the token type associated with this state. */
    public String getTokenType() { return tokenType; }

    /** Sets the token type for this state. */
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    /** Alias for setFinal. */
    public void setAccepting(boolean isAccepting) { this.isFinal = isAccepting; }

    /** Alias for isFinal. */
    public boolean isAccepting() { return isFinal; }

    /** Returns the states reachable via epsilon transitions. */
    public List<State> getEpsilonTransitions() {
        List<State> epsilonStates = new ArrayList<>();
        for (Transition transition : transitions) {
            if (transition.getSymbol() == null) {
                epsilonStates.add(transition.getToState());
            }
        }
        return Collections.unmodifiableList(epsilonStates);
    }

    /** Returns the states reachable via a transition with the given symbol. */
    public List<State> getTransitions(char symbol) {
        List<State> symbolStates = new ArrayList<>();
        for (Transition transition : transitions) {
            if (transition.getSymbol() != null && transition.getSymbol() == symbol) {
                symbolStates.add(transition.getToState());
            }
        }
        return Collections.unmodifiableList(symbolStates);
    }

    /** Adds a transition with a symbol (null = epsilon). */
    public void addTransition(Character symbol, State toState) {
        if (toState == null) {
            throw new IllegalArgumentException("Destination state cannot be null");
        }
        transitions.add(new Transition(symbol, toState));
    }

    /** Adds an epsilon transition. */
    public void addEpsilonTransition(State toState) {
        addTransition(null, toState);
    }

    /** Returns all transitions. */
    public List<Transition> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }

    /** String representation. */
    @Override
    public String toString() {
        return "State{" +
               "id=" + id +
               ", isFinal=" + isFinal +
               ", tokenType=" + tokenType +
               ", transitions=" + transitions.size() +
               '}';
    }

    /** Resets the static ID counter (mainly for testing purposes). */
    public static void resetIdCounter() { nextId = 0; }

    /** Returns the number of transitions from this state. */
    public int getTransitionCount() { return transitions.size(); }

    /** Checks if this state has any epsilon transitions. */
    public boolean hasEpsilonTransitions() {
        for (Transition transition : transitions) {
            if (transition.getSymbol() == null) {
                return true;
            }
        }
        return false;
    }

    /** Checks if this state has any transitions with the given symbol. */
    public boolean hasTransitions(char symbol) {
        for (Transition transition : transitions) {
            if (transition.getSymbol() != null && transition.getSymbol() == symbol) {
                return true;
            }
        }
        return false;
    }
}
