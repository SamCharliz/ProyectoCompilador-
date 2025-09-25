package com.compiler.parser.syntax;

import java.util.*;
import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.SymbolType;

/**
 * Calculates the FIRST and FOLLOW sets for a given grammar.
 * Main task of Practice 5.
 */
public class StaticAnalyzer {
    private final Grammar grammar;
    private final Map<Symbol, Set<Symbol>> firstSets;
    private final Map<Symbol, Set<Symbol>> followSets;
    
    // Símbolos especiales
    private final Symbol epsilonSymbol;
    private final Symbol endMarker;

    public StaticAnalyzer(Grammar grammar) {
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
        
        // Crear símbolos especiales
        this.epsilonSymbol = new Symbol("ε", SymbolType.TERMINAL);
        this.endMarker = new Symbol("$", SymbolType.TERMINAL);
        
        initializeFirstSets();
    }

    /**
     * Inicializa los conjuntos FIRST para todos los símbolos
     */
    private void initializeFirstSets() {
        // Para terminales: FIRST(symbol) = {symbol}
        for (Symbol terminal : grammar.getTerminals()) {
            Set<Symbol> firstSet = new HashSet<>();
            firstSet.add(terminal);
            firstSets.put(terminal, firstSet);
        }
        
        // Para épsilon
        Set<Symbol> epsilonFirst = new HashSet<>();
        epsilonFirst.add(epsilonSymbol);
        firstSets.put(epsilonSymbol, epsilonFirst);
        
        // Para no terminales: inicialmente vacíos
        for (Symbol nonTerminal : grammar.getNonTerminals()) {
            firstSets.put(nonTerminal, new HashSet<>());
        }
        
        // Para el símbolo de fin de cadena
        Set<Symbol> endMarkerFirst = new HashSet<>();
        endMarkerFirst.add(endMarker);
        firstSets.put(endMarker, endMarkerFirst);
    }

    /**
     * Calculates and returns the FIRST sets for all symbols.
     * @return A map from Symbol to its FIRST set.
     */
    public Map<Symbol, Set<Symbol>> getFirstSets() {
        boolean changed;
        
        do {
            changed = false;
            
            for (Production production : grammar.getProductions()) {
                Symbol lhs = production.getLeft();
                List<Symbol> rhs = production.getRight();
                
                Set<Symbol> firstLHS = firstSets.get(lhs);
                int originalSize = firstLHS.size();
                
                // Si la producción es A -> ε
                if (rhs.size() == 1 && rhs.get(0).name.equals("ε")) {
                    if (firstLHS.add(epsilonSymbol)) {
                        changed = true;
                    }
                    continue;
                }
                
                // Procesar cada símbolo en el lado derecho
                boolean allCanBeEpsilon = true;
                
                for (Symbol symbol : rhs) {
                    Set<Symbol> firstSymbol = firstSets.get(symbol);
                    if (firstSymbol == null) {
                        // Si el símbolo no está en firstSets, saltarlo
                        continue;
                    }
                    
                    // Agregar FIRST(symbol) - {ε} a FIRST(lhs)
                    for (Symbol s : firstSymbol) {
                        if (!s.name.equals("ε")) {
                            if (firstLHS.add(s)) {
                                changed = true;
                            }
                        }
                    }
                    
                    // Si el símbolo actual no puede ser ε, terminar
                    if (!firstSymbol.contains(epsilonSymbol)) {
                        allCanBeEpsilon = false;
                        break;
                    }
                }
                
                // Si todos los símbolos pueden ser ε, agregar ε a FIRST(lhs)
                if (allCanBeEpsilon) {
                    if (firstLHS.add(epsilonSymbol)) {
                        changed = true;
                    }
                }
                
                // Verificar si hubo cambios
                if (firstLHS.size() > originalSize) {
                    changed = true;
                }
            }
        } while (changed);
        
        return firstSets;
    }

    /**
     * Calculates and returns the FOLLOW sets for non-terminals.
     * @return A map from Symbol to its FOLLOW set.
     */
    public Map<Symbol, Set<Symbol>> getFollowSets() {
        // Asegurarse de que FIRST sets están calculados
        if (firstSets.isEmpty() || firstSets.values().stream().allMatch(Set::isEmpty)) {
            getFirstSets();
        }
        
        // Inicializar FOLLOW sets para no terminales
        for (Symbol nonTerminal : grammar.getNonTerminals()) {
            followSets.put(nonTerminal, new HashSet<>());
        }
        
        // Agregar $ al símbolo inicial
        Symbol startSymbol = grammar.getStartSymbol();
        followSets.get(startSymbol).add(endMarker);
        
        boolean changed;
        
        do {
            changed = false;
            
            for (Production production : grammar.getProductions()) {
                Symbol lhs = production.getLeft();
                List<Symbol> rhs = production.getRight();
                
                for (int i = 0; i < rhs.size(); i++) {
                    Symbol currentSymbol = rhs.get(i);
                    
                    // Solo procesar no terminales
                    if (currentSymbol.type != SymbolType.NON_TERMINAL) {
                        continue;
                    }
                    
                    Set<Symbol> followCurrent = followSets.get(currentSymbol);
                    int originalSize = followCurrent.size();
                    
                    // Verificar símbolos siguientes
                    boolean allCanBeEpsilon = true;
                    
                    for (int j = i + 1; j < rhs.size(); j++) {
                        Symbol nextSymbol = rhs.get(j);
                        Set<Symbol> firstNext = firstSets.get(nextSymbol);
                        
                        if (firstNext == null) {
                            // Si no hay FIRST set para este símbolo, continuar
                            continue;
                        }
                        
                        // Agregar FIRST(nextSymbol) - {ε} a FOLLOW(currentSymbol)
                        for (Symbol s : firstNext) {
                            if (!s.name.equals("ε")) {
                                if (followCurrent.add(s)) {
                                    changed = true;
                                }
                            }
                        }
                        
                        // Si el símbolo siguiente no puede ser ε, terminar
                        if (!firstNext.contains(epsilonSymbol)) {
                            allCanBeEpsilon = false;
                            break;
                        }
                    }
                    
                    // Si todos los símbolos siguientes pueden ser ε, agregar FOLLOW(lhs)
                    if (allCanBeEpsilon || i == rhs.size() - 1) {
                        Set<Symbol> followLHS = followSets.get(lhs);
                        for (Symbol s : followLHS) {
                            if (followCurrent.add(s)) {
                                changed = true;
                            }
                        }
                    }
                    
                    // Verificar si hubo cambios
                    if (followCurrent.size() > originalSize) {
                        changed = true;
                    }
                }
            }
        } while (changed);
        
        return followSets;
    }

    /**
     * Método auxiliar para obtener el símbolo épsilon
     */
    public Symbol getEpsilonSymbol() {
        return epsilonSymbol;
    }
    
    /**
     * Método auxiliar para obtener el símbolo de fin de cadena
     */
    public Symbol getEndMarker() {
        return endMarker;
    }

    /**
     * Método auxiliar para imprimir los conjuntos FIRST (para debugging)
     */
    public void printFirstSets() {
        System.out.println("=== FIRST Sets ===");
        for (Map.Entry<Symbol, Set<Symbol>> entry : firstSets.entrySet()) {
            System.out.print("FIRST(" + entry.getKey().name + ") = { ");
            for (Symbol s : entry.getValue()) {
                System.out.print(s.name + " ");
            }
            System.out.println("}");
        }
    }

    /**
     * Método auxiliar para imprimir los conjuntos FOLLOW (para debugging)
     */
    public void printFollowSets() {
        System.out.println("=== FOLLOW Sets ===");
        for (Map.Entry<Symbol, Set<Symbol>> entry : followSets.entrySet()) {
            System.out.print("FOLLOW(" + entry.getKey().name + ") = { ");
            for (Symbol s : entry.getValue()) {
                System.out.print(s.name + " ");
            }
            System.out.println("}");
        }
    }
}