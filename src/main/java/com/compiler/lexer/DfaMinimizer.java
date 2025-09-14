package com.compiler.lexer;

import java.util.*;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

public class DfaMinimizer {
    public DfaMinimizer() {
        // Constructor implementation
    }

    public static DFA minimizeDfa(DFA originalDfa, Set<Character> alphabet) {
        // Make DFA complete first
        DFA completeDfa = originalDfa.makeComplete();
        List<DfaState> allStates = completeDfa.getStates();
        
        // Sort states by ID for consistent ordering
        allStates.sort(Comparator.comparingInt(DfaState::getId));
        
        // Initialize table with all pairs
        Map<Pair, Boolean> table = new HashMap<>();
        
        // Mark initially distinguishable pairs (final vs non-final)
        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                DfaState s1 = allStates.get(i);
                DfaState s2 = allStates.get(j);
                Pair pair = new Pair(s1, s2);
                boolean distinguishable = s1.isFinal() != s2.isFinal();
                table.put(pair, distinguishable);
            }
        }
        
        // Iteratively mark distinguishable pairs
        boolean changed;
        do {
            changed = false;
            for (Pair pair : table.keySet()) {
                if (!table.get(pair)) { // If not yet distinguishable
                    for (char symbol : alphabet) {
                        DfaState next1 = pair.s1.getTransition(symbol);
                        DfaState next2 = pair.s2.getTransition(symbol);
                        
                        if (next1 != null && next2 != null) {
                            Pair nextPair = new Pair(next1, next2);
                            if (table.getOrDefault(nextPair, false)) {
                                table.put(pair, true);
                                changed = true;
                                break;
                            }
                        } else if (next1 != null || next2 != null) {
                            // One has transition, other doesn't
                            table.put(pair, true);
                            changed = true;
                            break;
                        }
                    }
                }
            }
        } while (changed);
        
        // Create partitions of equivalent states
        List<Set<DfaState>> partitions = createPartitions(allStates, table);
        
        // Build minimized DFA
        return buildMinimizedDfa(partitions, completeDfa, alphabet);
    }

    private static List<Set<DfaState>> createPartitions(List<DfaState> allStates, Map<Pair, Boolean> table) {
        Map<DfaState, DfaState> parent = new HashMap<>();
        
        // Initialize each state as its own parent
        for (DfaState state : allStates) {
            parent.put(state, state);
        }
        
        // Union states that are not distinguishable
        for (Pair pair : table.keySet()) {
            if (!table.get(pair)) {
                union(parent, pair.s1, pair.s2);
            }
        }
        
        // Group states by their root parent
        Map<DfaState, Set<DfaState>> groups = new HashMap<>();
        for (DfaState state : allStates) {
            DfaState root = find(parent, state);
            groups.computeIfAbsent(root, k -> new HashSet<>()).add(state);
        }
        
        return new ArrayList<>(groups.values());
    }

    private static DfaState find(Map<DfaState, DfaState> parent, DfaState state) {
        if (parent.get(state) != state) {
            parent.put(state, find(parent, parent.get(state))); // Path compression
        }
        return parent.get(state);
    }

    private static void union(Map<DfaState, DfaState> parent, DfaState s1, DfaState s2) {
        DfaState root1 = find(parent, s1);
        DfaState root2 = find(parent, s2);
        if (root1 != root2) {
            parent.put(root2, root1);
        }
    }

    private static DFA buildMinimizedDfa(List<Set<DfaState>> partitions, DFA originalDfa, Set<Character> alphabet) {
        Map<Set<DfaState>, DfaState> partitionToState = new HashMap<>();
        List<DfaState> newStates = new ArrayList<>();
        
        // Create new states for each partition
        for (Set<DfaState> partition : partitions) {
            DfaState newState = new DfaState(new HashSet<>());
            newState.setFinal(partition.iterator().next().isFinal());
            partitionToState.put(partition, newState);
            newStates.add(newState);
        }
        
        // Create transitions
        for (Set<DfaState> partition : partitions) {
            DfaState newState = partitionToState.get(partition);
            DfaState representative = partition.iterator().next();
            
            for (char symbol : alphabet) {
                DfaState target = representative.getTransition(symbol);
                if (target != null) {
                    // Find which partition contains the target state
                    for (Set<DfaState> targetPartition : partitions) {
                        if (targetPartition.contains(target)) {
                            newState.addTransition(symbol, partitionToState.get(targetPartition));
                            break;
                        }
                    }
                }
            }
        }
        
        // Find start state partition
        DfaState newStartState = null;
        for (Set<DfaState> partition : partitions) {
            if (partition.contains(originalDfa.startState)) {
                newStartState = partitionToState.get(partition);
                break;
            }
        }
        
        return new DFA(newStartState, newStates, alphabet);
    }

    private static class Pair {
        final DfaState s1;
        final DfaState s2;

        public Pair(DfaState s1, DfaState s2) {
            // Ensure canonical order (lower ID first)
            if (s1.getId() <= s2.getId()) {
                this.s1 = s1;
                this.s2 = s2;
            } else {
                this.s1 = s2;
                this.s2 = s1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return s1.getId() == pair.s1.getId() && s2.getId() == pair.s2.getId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(s1.getId(), s2.getId());
        }
    }
}