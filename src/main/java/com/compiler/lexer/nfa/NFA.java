package com.compiler.lexer.nfa;

/**
 * Represents a Non-deterministic Finite Automaton (NFA) with a start and end state.
 * <p>
 * An NFA is used in lexical analysis to model regular expressions and pattern matching.
 * This class encapsulates the start and end states of the automaton.
 */
public class NFA {
    /**
     * The initial (start) state of the NFA.
     */
    public final State startState;

    /**
     * The final (accepting) state of the NFA.
     */
    public final State endState;

    /**
     * Constructs a new NFA with the given start and end states.
     * @param start The initial state.
     * @param end The final (accepting) state.
     */
    public NFA(State start, State end) {
        if (start == null) {
            throw new IllegalArgumentException("Start state cannot be null");
        }
        if (end == null) {
            throw new IllegalArgumentException("End state cannot be null");
        }
        this.startState = start;
        this.endState = end;
    }

    /**
     * Returns the initial (start) state of the NFA.
     * @return the start state
     */
    public State getStartState() {
        return startState;
    }

    /**
     * Returns the final (accepting) state of the NFA.
     * @return the end state
     */
    public State getEndState() {
        return endState;
    }

    /**
     * Sets whether the end state is an accepting state.
     * @param isAccepting true if the end state should be accepting, false otherwise
     */
    public void setEndStateAccepting(boolean isAccepting) {
        endState.setAccepting(isAccepting);
    }

    /**
     * Checks if the end state is an accepting state.
     * @return true if the end state is accepting, false otherwise
     */
    public boolean isEndStateAccepting() {
        return endState.isAccepting();
    }

    /**
     * Returns a string representation of the NFA.
     * @return string containing start and end state information
     */
    @Override
    public String toString() {
        return "NFA{" +
               "startState=" + startState +
               ", endState=" + endState +
               ", endStateAccepting=" + endState.isAccepting() +
               '}';
    }

    /**
     * Creates a basic NFA for a single character.
     * @param c The character for the transition
     * @return A new NFA that recognizes the single character
     */
    public static NFA createForCharacter(char c) {
        State start = new State();
        State end = new State();
        end.setAccepting(true);
        start.addTransition(c, end);
        return new NFA(start, end);
    }

    /**
     * Creates a basic NFA for epsilon transition.
     * @return A new NFA with an epsilon transition
     */
    public static NFA createForEpsilon() {
        State start = new State();
        State end = new State();
        end.setAccepting(true);
        start.addEpsilonTransition(end);
        return new NFA(start, end);
    }

    /**
     * Performs the union operation between two NFAs (OR operation).
     * @param nfa1 First NFA
     * @param nfa2 Second NFA
     * @return A new NFA that represents nfa1 | nfa2
     */
    public static NFA union(NFA nfa1, NFA nfa2) {
        State start = new State();
        State end = new State();
        end.setAccepting(true);

        // Add epsilon transitions from new start to both NFAs
        start.addEpsilonTransition(nfa1.startState);
        start.addEpsilonTransition(nfa2.startState);

        // Add epsilon transitions from both NFAs' end states to new end state
        nfa1.endState.setAccepting(false);
        nfa2.endState.setAccepting(false);
        nfa1.endState.addEpsilonTransition(end);
        nfa2.endState.addEpsilonTransition(end);

        return new NFA(start, end);
    }

    /**
     * Performs the concatenation operation between two NFAs.
     * @param nfa1 First NFA
     * @param nfa2 Second NFA
     * @return A new NFA that represents nfa1 followed by nfa2
     */
    public static NFA concatenate(NFA nfa1, NFA nfa2) {
        // Connect end of nfa1 to start of nfa2
        nfa1.endState.setAccepting(false);
        nfa1.endState.addEpsilonTransition(nfa2.startState);
        
        return new NFA(nfa1.startState, nfa2.endState);
    }

    /**
     * Performs the Kleene star operation on an NFA (zero or more repetitions).
     * @param nfa The NFA to apply Kleene star to
     * @return A new NFA that represents nfa*
     */
    public static NFA kleeneStar(NFA nfa) {
        State start = new State();
        State end = new State();
        end.setAccepting(true);

        // Epsilon from new start to nfa start and to new end
        start.addEpsilonTransition(nfa.startState);
        start.addEpsilonTransition(end);

        // Epsilon from nfa end to nfa start and to new end
        nfa.endState.setAccepting(false);
        nfa.endState.addEpsilonTransition(nfa.startState);
        nfa.endState.addEpsilonTransition(end);

        return new NFA(start, end);
    }

    /**
     * Performs the plus operation on an NFA (one or more repetitions).
     * @param nfa The NFA to apply plus to
     * @return A new NFA that represents nfa+
     */
    public static NFA plus(NFA nfa) {
        State start = new State();
        State end = new State();
        end.setAccepting(true);

        // Connect start to nfa start
        start.addEpsilonTransition(nfa.startState);

        // From nfa end, go back to nfa start or to end
        nfa.endState.setAccepting(false);
        nfa.endState.addEpsilonTransition(nfa.startState);
        nfa.endState.addEpsilonTransition(end);

        return new NFA(start, end);
    }

    /**
     * Performs the optional operation on an NFA (zero or one occurrence).
     * @param nfa The NFA to make optional
     * @return A new NFA that represents nfa?
     */
    public static NFA optional(NFA nfa) {
        State start = new State();
        State end = new State();
        end.setAccepting(true);

        // Epsilon from start to nfa start and directly to end
        start.addEpsilonTransition(nfa.startState);
        start.addEpsilonTransition(end);

        // From nfa end to final end
        nfa.endState.setAccepting(false);
        nfa.endState.addEpsilonTransition(end);

        return new NFA(start, end);
    }
}