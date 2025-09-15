package com.compiler;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.compiler.lexer.DfaMinimizer;
import com.compiler.lexer.DfaSimulator;
import com.compiler.lexer.NfaToDfaConverter;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;

/**
 * Unit tests for DFA minimization functionality.
 * 
 * <p>This test class verifies that a DFA generated from a regular expression
 * is correctly minimized and continues to accept valid strings while rejecting invalid ones.
 */
public class DfaMinimizationTest {

    /**
     * Tests DFA minimization for the regular expression "a(b*|c+)?d".
     * 
     * <p>The test ensures that the minimized DFA accepts all strings that match
     * the regex and rejects strings that do not.
     */
    @Test
    public void testMinimization_abd() {
        String regex = "a(b*|c+)?d";

        // Parse regex into NFA
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);

        // Define alphabet
        Set<Character> alphabet = new HashSet<>();
        alphabet.add('a');
        alphabet.add('b');
        alphabet.add('c');
        alphabet.add('d');

        // Convert NFA to DFA
        DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);

        // Minimize DFA
        DFA minimized = DfaMinimizer.minimizeDfa(dfa, alphabet);

        // Create simulator
        DfaSimulator dfaSimulator = new DfaSimulator();

        // Test strings that should be accepted
        assertTrue(dfaSimulator.simulate(minimized, "abd"), "Minimized DFA should accept 'abd'");
        assertTrue(dfaSimulator.simulate(minimized, "acd"), "Minimized DFA should accept 'acd'");
        assertTrue(dfaSimulator.simulate(minimized, "abbbd"), "Minimized DFA should accept 'abbbd'");
        assertTrue(dfaSimulator.simulate(minimized, "acccd"), "Minimized DFA should accept 'acccd'");
        assertTrue(dfaSimulator.simulate(minimized, "ad"), "Minimized DFA should accept 'ad'");

        // Test strings that should be rejected
        assertFalse(dfaSimulator.simulate(minimized, "a"), "Minimized DFA should not accept 'a'");
        assertFalse(dfaSimulator.simulate(minimized, "d"), "Minimized DFA should not accept 'd'");
    }
}
