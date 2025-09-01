package com.compiler.lexer.nfa;

/**
 * Represents a transition in a Non-deterministic Finite Automaton (NFA).
 * Each transition consists of a symbol and a destination state.
 *
 * <p>
 * The transition occurs when the automaton reads the specified symbol,
 * moving from the current state to the given destination state.
 * </p>
 */
public class Transition {
    /**
     * The symbol that triggers this transition. Null for epsilon transitions.
     */
    public final Character symbol;

    /**
     * The destination state for this transition.
     */
    public final State to;

    /**
     * Constructs a new transition with the given symbol and destination state.
     * @param symbol The symbol for the transition (null for epsilon).
     * @param toState The destination state.
     */
    public Transition(Character symbol, State toState) {
        if (toState == null) {
            throw new IllegalArgumentException("Destination state cannot be null");
        }
        this.symbol = symbol;
        this.to = toState;
    }

    /**
     * Checks if this is an epsilon transition.
     * @return true if this is an epsilon transition, false otherwise
     */
    public boolean isEpsilon() {
        return symbol == null;
    }

    /**
     * Gets the symbol that triggers this transition.
     * @return the transition symbol, or null for epsilon transitions
     */
    public Character getSymbol() {
        return symbol;
    }

    /**
     * Gets the destination state of this transition.
     * @return the destination state
     */
    public State getToState() {
        return to;
    }

    /**
     * Returns a string representation of the transition.
     * @return string showing the symbol and destination state
     */
    @Override
    public String toString() {
        return "Transition{" +
               "symbol=" + (symbol == null ? "ε" : symbol) +
               ", to=" + to.id +
               '}';
    }

    /**
     * Checks if this transition is equal to another object.
     * Two transitions are equal if they have the same symbol and destination state.
     * @param obj the object to compare with
     * @return true if the transitions are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Transition that = (Transition) obj;
        if (symbol == null) {
            return that.symbol == null && to.equals(that.to);
        }
        return symbol.equals(that.symbol) && to.equals(that.to);
    }

    /**
     * Returns a hash code value for the transition.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = symbol != null ? symbol.hashCode() : 0;
        result = 31 * result + to.hashCode();
        return result;
    }

    /**
     * Creates a new epsilon transition to the given state.
     * @param toState the destination state
     * @return a new epsilon transition
     */
    public static Transition createEpsilon(State toState) {
        return new Transition(null, toState);
    }

    /**
     * Creates a new symbol transition to the given state.
     * @param symbol the transition symbol
     * @param toState the destination state
     * @return a new symbol transition
     */
    public static Transition createSymbolTransition(char symbol, State toState) {
        return new Transition(symbol, toState);
    }
}