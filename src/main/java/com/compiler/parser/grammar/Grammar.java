/**
 * Provides the representation and parsing logic for a context-free grammar (CFG).
 * 
 * A Grammar consists of non-terminals, terminals, productions, and a start symbol.
 * It can be constructed from a string definition using a simple BNF-like format.
 * 
 * Example grammar definition:
 * S -> A B | ε
 * A -> a
 * B -> b
 */
package com.compiler.parser.grammar;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a complete context-free grammar.
 */
public class Grammar {
    private final Set<Symbol> nonTerminals;
    private final Set<Symbol> terminals;
    private final List<Production> productions;
    private final Symbol startSymbol;

    public Grammar(String grammarDefinition) {
        if (grammarDefinition == null || grammarDefinition.trim().isEmpty()) {
            throw new IllegalArgumentException("Grammar definition cannot be null or empty.");
        }

        GrammarData data = parseGrammarDefinition(grammarDefinition);

        this.nonTerminals = java.util.Collections.unmodifiableSet(data.nonTerminals);
        this.terminals = java.util.Collections.unmodifiableSet(data.terminals);
        this.productions = java.util.Collections.unmodifiableList(data.productions);
        this.startSymbol = data.startSymbol;

        validateProductions(this.productions);
    }

    private static class GrammarData {
        Set<Symbol> nonTerminals;
        Set<Symbol> terminals;
        List<Production> productions;
        Symbol startSymbol;
    }

    private GrammarData parseGrammarDefinition(String grammarDefinition) {
        Set<String> nonTerminalNames = new java.util.LinkedHashSet<>();
        Set<String> terminalNames = new java.util.LinkedHashSet<>();
        List<Production> tempProductions = new java.util.ArrayList<>();
        Map<String, List<List<String>>> productionMap = new java.util.LinkedHashMap<>();

        parseLines(grammarDefinition, nonTerminalNames, productionMap);
        collectTerminals(nonTerminalNames, productionMap, terminalNames);

        Map<String, Symbol> symbolMap = buildSymbolMap(nonTerminalNames, terminalNames);
        tempProductions.addAll(buildProductions(productionMap, symbolMap));

        GrammarData data = new GrammarData();
        data.nonTerminals = nonTerminalNames.stream().map(symbolMap::get).collect(java.util.stream.Collectors.toSet());
        data.terminals = terminalNames.stream().map(symbolMap::get).collect(java.util.stream.Collectors.toSet());
        data.productions = tempProductions;
        data.startSymbol = symbolMap.get(nonTerminalNames.iterator().next());
        return data;
    }

    private void parseLines(String grammarDefinition, Set<String> nonTerminalNames, Map<String, List<List<String>>> productionMap) {
        String[] lines = grammarDefinition.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("->");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid production line: " + line);
            }
            String lhs = parts[0].trim();
            nonTerminalNames.add(lhs);

            String rhs = parts[1].trim();
            String[] alternatives = rhs.split("\\|");
            for (String alt : alternatives) {
                List<String> symbols = new java.util.ArrayList<>();
                for (String symbol : alt.trim().split("\\s+")) {
                    if (!symbol.isEmpty()) {
                        symbols.add(symbol);
                    }
                }
                productionMap.computeIfAbsent(lhs, k -> new java.util.ArrayList<>()).add(symbols);
            }
        }
    }

    private void collectTerminals(Set<String> nonTerminalNames, Map<String, List<List<String>>> productionMap, Set<String> terminalNames) {
        for (List<List<String>> rhsList : productionMap.values()) {
            for (List<String> prod : rhsList) {
                for (String symbol : prod) {
                    if (!nonTerminalNames.contains(symbol) && !"ε".equals(symbol)) {
                        terminalNames.add(symbol);
                    }
                }
            }
        }
    }

    private Map<String, Symbol> buildSymbolMap(Set<String> nonTerminalNames, Set<String> terminalNames) {
        Map<String, Symbol> symbolMap = new java.util.HashMap<>();
        for (String nt : nonTerminalNames) {
            symbolMap.put(nt, new Symbol(nt, SymbolType.NON_TERMINAL));
        }
        for (String t : terminalNames) {
            symbolMap.put(t, new Symbol(t, SymbolType.TERMINAL));
        }
        symbolMap.put("ε", new Symbol("ε", SymbolType.TERMINAL));
        return symbolMap;
    }

    private List<Production> buildProductions(Map<String, List<List<String>>> productionMap, Map<String, Symbol> symbolMap) {
        List<Production> prodList = new java.util.ArrayList<>();
        for (String lhs : productionMap.keySet()) {
            Symbol left = symbolMap.get(lhs);
            for (List<String> rhs : productionMap.get(lhs)) {
                List<Symbol> right = new java.util.ArrayList<>();
                for (String s : rhs) {
                    Symbol sym = symbolMap.get(s);
                    if (sym == null) {
                        throw new IllegalArgumentException("Undefined symbol: " + s);
                    }
                    right.add(sym);
                }
                prodList.add(new Production(left, right));
            }
        }
        return prodList;
    }

    private void validateProductions(List<Production> productions) {
        for (Production p : productions) {
            if (p.getLeft() == null || p.getRight().contains(null)) {
                throw new IllegalArgumentException("Production contains undefined symbol.");
            }
        }
    }

    public Set<Symbol> getNonTerminals() {
        return nonTerminals;
    }

    public Set<Symbol> getTerminals() {
        return terminals;
    }

    public List<Production> getProductions() {
        return productions;
    }

    public Symbol getStartSymbol() {
        return startSymbol;
    }
}