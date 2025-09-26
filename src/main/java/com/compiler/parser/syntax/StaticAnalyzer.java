package com.compiler.parser.syntax;

import java.util.*;
import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.SymbolType;

/**
 * Calculates the FIRST and FOLLOW sets for a given grammar.
 * This is the main task of Practice 5.
 * <p>
 * The class supports:
 * - Calculating FIRST sets for terminals and non-terminals.
 * - Calculating FOLLOW sets for non-terminals.
 * - Utility methods for checking nullability and sequences.
 * - Debugging methods to print FIRST and FOLLOW sets.
 */
public class StaticAnalyzer {
    
    /** The grammar to analyze */
    private final Grammar grammar;
    
    /** Maps each symbol to its FIRST set */
    private final Map<Symbol, Set<Symbol>> firstSets;
    
    /** Maps each symbol to its FOLLOW set */
    private final Map<Symbol, Set<Symbol>> followSets;
    
    /** Special epsilon symbol representing the empty string */
    private final Symbol epsilonSymbol;
    
    /** Special end-of-input marker ($) */
    private final Symbol endMarker;

    /**
     * Constructs a StaticAnalyzer for a given grammar.
     * Initializes FIRST sets and validates the grammar.
     *
     * @param grammar the grammar to analyze
     * @throws NullPointerException if grammar is null
     */
    public StaticAnalyzer(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar, "Grammar cannot be null");
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
        
        this.epsilonSymbol = new Symbol("ε", SymbolType.TERMINAL);
        this.endMarker = new Symbol("$", SymbolType.TERMINAL);
        
        validateGrammar();
        initializeFirstSets();
    }

    /**
     * Validates that the grammar has a start symbol and at least one production.
     *
     * @throws IllegalArgumentException if the grammar is invalid
     */
    private void validateGrammar() {
        if (grammar.getStartSymbol() == null) {
            throw new IllegalArgumentException("Grammar must have a start symbol");
        }
        if (grammar.getProductions().isEmpty()) {
            throw new IllegalArgumentException("Grammar must have at least one production");
        }
    }

    /**
     * Initializes the FIRST sets for all symbols:
     * - For terminals, FIRST(symbol) = {symbol}
     * - For epsilon, FIRST(ε) = {ε}
     * - For non-terminals, initially empty
     * - For end marker, FIRST($) = {$}
     */
    private void initializeFirstSets() {
        for (Symbol terminal : grammar.getTerminals()) {
            Set<Symbol> firstSet = new HashSet<>();
            firstSet.add(terminal);
            firstSets.put(terminal, firstSet);
        }
        
        Set<Symbol> epsilonFirst = new HashSet<>();
        epsilonFirst.add(epsilonSymbol);
        firstSets.put(epsilonSymbol, epsilonFirst);
        
        for (Symbol nonTerminal : grammar.getNonTerminals()) {
            firstSets.put(nonTerminal, new HashSet<>());
        }
        
        Set<Symbol> endMarkerFirst = new HashSet<>();
        endMarkerFirst.add(endMarker);
        firstSets.put(endMarker, endMarkerFirst);
    }

    /**
     * Calculates and returns the FIRST sets for all symbols.
     * Iteratively updates FIRST sets until no more changes occur.
     *
     * @return an unmodifiable map from Symbol to its FIRST set
     */
    public Map<Symbol, Set<Symbol>> getFirstSets() {
        boolean changed;
        int iteration = 0;
        
        do {
            changed = false;
            iteration++;
            
            for (Production production : grammar.getProductions()) {
                Symbol lhs = production.getLeft();
                List<Symbol> rhs = production.getRight();
                
                Set<Symbol> firstLHS = firstSets.get(lhs);
                
                if (rhs.isEmpty() || (rhs.size() == 1 && rhs.get(0).equals(epsilonSymbol))) {
                    if (firstLHS.add(epsilonSymbol)) changed = true;
                    continue;
                }
                
                boolean allCanBeEpsilon = true;
                
                for (Symbol symbol : rhs) {
                    Set<Symbol> firstSymbol = firstSets.get(symbol);
                    if (firstSymbol == null) {
                        allCanBeEpsilon = false;
                        break;
                    }
                    
                    for (Symbol s : firstSymbol) {
                        if (!s.equals(epsilonSymbol) && firstLHS.add(s)) changed = true;
                    }
                    
                    if (!firstSymbol.contains(epsilonSymbol)) {
                        allCanBeEpsilon = false;
                        break;
                    }
                }
                
                if (allCanBeEpsilon && firstLHS.add(epsilonSymbol)) changed = true;
            }
            
            if (changed) System.out.println("FIRST Sets - Iteration " + iteration + ": Changes detected");
            
        } while (changed);
        
        System.out.println("FIRST Sets calculation completed in " + iteration + " iterations");
        return Collections.unmodifiableMap(firstSets);
    }

    /**
     * Calculates and returns the FOLLOW sets for all non-terminals.
     * Iteratively updates FOLLOW sets until no more changes occur.
     *
     * @return an unmodifiable map from Symbol to its FOLLOW set
     */
    public Map<Symbol, Set<Symbol>> getFollowSets() {
        if (grammar.getNonTerminals().stream().noneMatch(nt -> !firstSets.get(nt).isEmpty())) {
            System.out.println("Calculating FIRST sets before FOLLOW sets...");
            getFirstSets();
        }
        
        for (Symbol nonTerminal : grammar.getNonTerminals()) {
            followSets.put(nonTerminal, new HashSet<>());
        }
        
        followSets.get(grammar.getStartSymbol()).add(endMarker);
        
        boolean changed;
        int iteration = 0;
        
        do {
            changed = false;
            iteration++;
            
            for (Production production : grammar.getProductions()) {
                Symbol lhs = production.getLeft();
                List<Symbol> rhs = production.getRight();
                
                for (int i = 0; i < rhs.size(); i++) {
                    Symbol currentSymbol = rhs.get(i);
                    if (currentSymbol.type != SymbolType.NON_TERMINAL) continue;
                    
                    Set<Symbol> followCurrent = followSets.get(currentSymbol);
                    boolean allCanBeEpsilon = true;
                    
                    for (int j = i + 1; j < rhs.size(); j++) {
                        Symbol nextSymbol = rhs.get(j);
                        Set<Symbol> firstNext = firstSets.get(nextSymbol);
                        if (firstNext == null) {
                            allCanBeEpsilon = false;
                            break;
                        }
                        
                        for (Symbol s : firstNext) {
                            if (!s.equals(epsilonSymbol) && followCurrent.add(s)) changed = true;
                        }
                        
                        if (!firstNext.contains(epsilonSymbol)) {
                            allCanBeEpsilon = false;
                            break;
                        }
                    }
                    
                    if (allCanBeEpsilon) {
                        Set<Symbol> followLHS = followSets.get(lhs);
                        for (Symbol s : followLHS) {
                            if (followCurrent.add(s)) changed = true;
                        }
                    }
                }
            }
            
            if (changed) System.out.println("FOLLOW Sets - Iteration " + iteration + ": Changes detected");
            
        } while (changed);
        
        System.out.println("FOLLOW Sets calculation completed in " + iteration + " iterations");
        return Collections.unmodifiableMap(followSets);
    }

    /** @return the epsilon symbol (ε) */
    public Symbol getEpsilonSymbol() {
        return epsilonSymbol;
    }
    
    /** @return the end-of-input marker ($) */
    public Symbol getEndMarker() {
        return endMarker;
    }

    /** Prints all FIRST sets (for debugging purposes) */
    public void printFirstSets() {
        System.out.println("=== FIRST Sets ===");
        List<Symbol> nonTerminals = new ArrayList<>(grammar.getNonTerminals());
        nonTerminals.sort(Comparator.comparing(s -> s.name));
        
        for (Symbol symbol : nonTerminals) {
            Set<Symbol> firstSet = firstSets.get(symbol);
            System.out.printf("FIRST(%s) = { %s }%n", 
                symbol.name, 
                firstSet.stream()
                        .map(s -> s.name)
                        .sorted()
                        .reduce((a, b) -> a + ", " + b)
                        .orElse(""));
        }
        System.out.println();
    }

    /** Prints all FOLLOW sets (for debugging purposes) */
    public void printFollowSets() {
        System.out.println("=== FOLLOW Sets ===");
        List<Symbol> nonTerminals = new ArrayList<>(grammar.getNonTerminals());
        nonTerminals.sort(Comparator.comparing(s -> s.name));
        
        for (Symbol symbol : nonTerminals) {
            Set<Symbol> followSet = followSets.get(symbol);
            System.out.printf("FOLLOW(%s) = { %s }%n", 
                symbol.name,
                followSet.stream()
                         .map(s -> s.name)
                         .sorted()
                         .reduce((a, b) -> a + ", " + b)
                         .orElse(""));
        }
        System.out.println();
    }

    /** Prints all sets (FIRST and FOLLOW) */
    public void printAllSets() {
        printFirstSets();
        printFollowSets();
    }

    /**
     * Checks if a symbol is nullable (can derive ε)
     * @param symbol the symbol to check
     * @return true if the symbol can derive ε, false otherwise
     */
    public boolean isNullable(Symbol symbol) {
        if (symbol.type == SymbolType.TERMINAL) return symbol.equals(epsilonSymbol);
        Set<Symbol> firstSet = firstSets.get(symbol);
        return firstSet != null && firstSet.contains(epsilonSymbol);
    }

    /**
     * Calculates the FIRST set of a sequence of symbols.
     * @param sequence list of symbols
     * @return the FIRST set of the sequence
     */
    public Set<Symbol> getFirstSetForSequence(List<Symbol> sequence) {
        Set<Symbol> result = new HashSet<>();
        if (sequence.isEmpty()) {
            result.add(epsilonSymbol);
            return result;
        }
        
        boolean allCanBeEpsilon = true;
        for (Symbol symbol : sequence) {
            Set<Symbol> firstSymbol = firstSets.get(symbol);
            if (firstSymbol == null) {
                allCanBeEpsilon = false;
                break;
            }
            for (Symbol s : firstSymbol) {
                if (!s.equals(epsilonSymbol)) result.add(s);
            }
            if (!firstSymbol.contains(epsilonSymbol)) {
                allCanBeEpsilon = false;
                break;
            }
        }
        
        if (allCanBeEpsilon) result.add(epsilonSymbol);
        return result;
    }
}
