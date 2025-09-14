package com.compiler.lexer.nfa;

/**
 * Represents a Non-deterministic Finite Automaton (NFA) with a start and end state.
 * <p>
 * An NFA is used in lexical analysis to model regular expressions and pattern matching.
 * This class encapsulates the start and end states of the automaton.
 */
public class NFA {
    /** The initial (start) state of the NFA. */
    public final State startState;

    /** The final (accepting) state of the NFA. */
    public final State endState;

    /**
     * Constructs a new NFA with the given start and end states.
     * @param start The initial state.
     * @param end The final (accepting) state.
     */
    public NFA(State start, State end) {
        if (start == null) throw new IllegalArgumentException("Start state cannot be null");
        if (end == null) throw new IllegalArgumentException("End state cannot be null");
        this.startState = start;
        this.endState = end;
    }

    /** Returns the start state. */
    public State getStartState() { return startState; }

    /** Returns the end state. */
    public State getEndState() { return endState; }

    /** Sets whether the end state is accepting. */
    public void setEndStateAccepting(boolean isAccepting) { endState.setAccepting(isAccepting); }

    /** Checks if the end state is accepting. */
    public boolean isEndStateAccepting() { return endState.isAccepting(); }

    @Override
    public String toString() {
        return "NFA{" +
               "startState=" + startState +
               ", endState=" + endState +
               ", endStateAccepting=" + endState.isAccepting() +
               '}';
    }

    /** Creates a basic NFA for a single character. */
    public static NFA createForCharacter(char c) {
        State start = new State();
        State end = new State();
        end.setAccepting(true);
        start.addTransition(c, end);
        return new NFA(start, end);
    }

    /** Creates a basic NFA for epsilon transition. */
    public static NFA createForEpsilon() {
        State start = new State();
        State end = new State();
        end.setAccepting(true);
        start.addEpsilonTransition(end);
        return new NFA(start, end);
    }

    /** Union (OR) operation. */
    public static NFA union(NFA nfa1, NFA nfa2) {
        State start = new State();
        State end = new State();
        end.setAccepting(true);

        start.addEpsilonTransition(nfa1.startState);
        start.addEpsilonTransition(nfa2.startState);

        nfa1.endState.setAccepting(false);
        nfa2.endState.setAccepting(false);
        nfa1.endState.addEpsilonTransition(end);
        nfa2.endState.addEpsilonTransition(end);

        return new NFA(start, end);
    }

    /** Concatenation operation. */
    public static NFA concatenate(NFA nfa1, NFA nfa2) {
        nfa1.endState.setAccepting(false);
        nfa1.endState.addEpsilonTransition(nfa2.startState);
        return new NFA(nfa1.startState, nfa2.endState);
    }

    /** Kleene star operation. */
    public static NFA kleeneStar(NFA nfa) {
        State start = new State();
        State end = new State();
        end.setAccepting(true);

        start.addEpsilonTransition(nfa.startState);
        start.addEpsilonTransition(end);

        nfa.endState.setAccepting(false);
        nfa.endState.addEpsilonTransition(nfa.startState);
        nfa.endState.addEpsilonTransition(end);

        return new NFA(start, end);
    }

    /** Plus operation. */
    public static NFA plus(NFA nfa) {
        State start = new State();
        State end = new State();
        end.setAccepting(true);

        start.addEpsilonTransition(nfa.startState);

        nfa.endState.setAccepting(false);
        nfa.endState.addEpsilonTransition(nfa.startState);
        nfa.endState.addEpsilonTransition(end);

        return new NFA(start, end);
    }

    /** Optional operation. */
    public static NFA optional(NFA nfa) {
        State start = new State();
        State end = new State();
        end.setAccepting(true);

        start.addEpsilonTransition(nfa.startState);
        start.addEpsilonTransition(end);

        nfa.endState.setAccepting(false);
        nfa.endState.addEpsilonTransition(end);

        return new NFA(start, end);
    }
}
