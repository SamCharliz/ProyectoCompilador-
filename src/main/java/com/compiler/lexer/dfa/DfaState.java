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

    /** Unique identifier for this DFA state */
    public final int id;

    /** The set of NFA states this DFA state represents */
    private final Set<State> nfaStates;

    /** Indicates whether this DFA state is a final (accepting) state */
    private boolean isFinal;

    /** Token type for this DFA state (null if not final) */
    private String tokenType;

    /** Map of input symbols to destination DFA states */
    private final Map<Character, DfaState> transitions;

    /** Constructs a new DFA state */
    public DfaState(Set<State> nfaStates) {
        this.id = nextId++;
        this.nfaStates = nfaStates;
        this.transitions = new HashMap<>();
        this.isFinal = nfaStates.stream().anyMatch(State::isFinal);
        // Asignar tokenType si alguna NFA state es final
        this.tokenType = nfaStates.stream()
                .filter(State::isFinal)
                .map(State::getTokenType)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /** Retorna todas las transiciones desde este estado */
    public Map<Character, DfaState> getTransitions() {
        return new HashMap<>(transitions);
    }

    /** Agrega una transición a otro estado en un símbolo dado */
    public void addTransition(Character symbol, DfaState toState) {
        if (symbol == null) throw new IllegalArgumentException("Symbol cannot be null for DFA transitions");
        transitions.put(symbol, toState);
    }

    /** Igualdad basada en el conjunto de NFA states */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DfaState other = (DfaState) obj;
        return Objects.equals(nfaStates, other.nfaStates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nfaStates);
    }

    /** Representación en cadena del estado */
    @Override
    public String toString() {
        return "DfaState{" +
               "id=" + id +
               ", isFinal=" + isFinal +
               ", tokenType=" + tokenType +
               ", transitions=" + transitions.keySet() +
               '}';
    }

    /** Setea la finalización del estado y opcionalmente el tokenType */
    public void setFinal(boolean isFinal, String tokenType) {
        this.isFinal = isFinal;
        this.tokenType = tokenType;
    }

    /** Solo cambia la finalización sin tokenType */
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public String getTokenType() {
        return tokenType;
    }

    public DfaState getTransition(char symbol) {
        return transitions.get(symbol);
    }

    public Set<State> getNfaStates() {
        return nfaStates;
    }

    public int getId() {
        return id;
    }

    public boolean hasTransition(char symbol) {
        return transitions.containsKey(symbol);
    }

    public int getTransitionCount() {
        return transitions.size();
    }

    /** Reinicia el contador de IDs para reconstruir DFA */
    public static void resetIdCounter() {
        nextId = 0;
    }
}
