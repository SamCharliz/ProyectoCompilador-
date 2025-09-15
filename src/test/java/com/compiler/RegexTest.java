package com.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.DfaSimulator;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.NfaSimulator;
import com.compiler.lexer.NfaToDfaConverter;
import com.compiler.lexer.regex.RegexParser;

/**
 * Unit tests for verifying the correctness of regex parsing and simulation
 * across NFA, DFA, and minimized DFA representations.
 * 
 * <p>This class uses a parameterized testing approach to validate that a regex
 * correctly accepts or rejects given input strings, and ensures consistency
 * between NFA simulation, DFA simulation, and DFA minimization.
 */
public class RegexTest {

    /**
     * Runs a single test case for a given regex and input string.
     * 
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Parses the regex into an NFA and sets the final state.</li>
     *   <li>Simulates the NFA on the input string.</li>
     *   <li>Builds the alphabet from the given symbols and converts the NFA to a DFA.</li>
     *   <li>Simulates the DFA on the input string.</li>
     *   <li>Minimizes the DFA and simulates the minimized DFA.</li>
     *   <li>Prints detailed information including regex, input, expected value,
     *       NFA/DFA results, number of states, and transitions.</li>
     *   <li>Asserts that all three simulations match the expected result.</li>
     * </ol>
     *
     * @param regex   The regular expression to test.
     * @param input   The input string to validate.
     * @param expected The expected result (true if the string should be accepted, false otherwise).
     * @param symbols The set of characters forming the alphabet for DFA conversion.
     */
    private void runTest(String regex, String input, boolean expected, char... symbols) {
        System.out.println("======================================");

        // Parse regex -> NFA
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.getEndState().setFinal(true);

        // NFA simulation
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);

        // Build alphabet and convert to DFA
        Set<Character> alphabet = new HashSet<>();
        for (char c : symbols) alphabet.add(c);
        DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
        DfaSimulator dfaSimulator = new DfaSimulator();
        boolean actualDfa = dfaSimulator.simulate(dfa, input);

        // Minimize DFA
        DFA minDFA = dfa.minimize();
        boolean actualMinDfa = dfaSimulator.simulate(minDFA, input);

        // Print details
        System.out.println("Regex: '" + regex + "'");
        System.out.println("Input: '" + input + "'");
        System.out.println("Expected: " + expected);
        System.out.println("NFA Result: " + actualNfa);
        System.out.println("DFA original - States: " + dfa.getStates().size());
        dfa.printTransitions();
        System.out.println("Minimized DFA - States: " + minDFA.getStates().size());
        minDFA.printTransitions();
        System.out.println("DFA Result: " + actualDfa);
        System.out.println("Minimized DFA Result: " + actualMinDfa);
        System.out.println("======================================\n");

        // Assertions
        assertEquals(expected, actualNfa, "NFA failed for input: '" + input + "'");
        assertEquals(expected, actualDfa, "DFA failed for input: '" + input + "'");
        assertEquals(expected, actualMinDfa, "Minimized DFA failed for input: '" + input + "'");
    }



    @ParameterizedTest
    @CsvSource({
        "a,      true",
        "aa,     true",
        "aaaa,   true",
        "'',     false",
        "b,      false"
    })
    void testPlusOperator(String input, boolean expected) {
        runTest("a+", input, expected, 'a');
    }

    @ParameterizedTest
    @CsvSource({
        "'',     true",
        "a,      true",
        "aa,     false",
        "b,      false"
    })
    void testOptionalOperator(String input, boolean expected) {
        runTest("a?", input, expected, 'a');
    }

    @ParameterizedTest
    @CsvSource({
        "a,      true",
        "b,      true",
        "'',     false",
        "c,      false",
        "ab,     false"
    })
    void testUnionOperator(String input, boolean expected) {
        runTest("a|b", input, expected, 'a', 'b');
    }

    @ParameterizedTest
    @CsvSource({
        "ab,     true",
        "c,      true",
        "a,      false",
        "b,      false",
        "ac,     false"
    })
    void testPrecedenceConcatOverUnion(String input, boolean expected) {
        runTest("ab|c", input, expected, 'a', 'b', 'c');
    }

    @ParameterizedTest
    @CsvSource({
        "ac,     true",
        "abc,    true",
        "abbbc,  true",
        "a,      false",
        "c,      false",
        "ab,     false",
        "bc,     false"
    })
    void testPrecedenceKleeneOverConcat(String input, boolean expected) {
        runTest("ab*c", input, expected, 'a', 'b', 'c');
    }

    @ParameterizedTest
    @CsvSource({
        "'',     true",
        "a,      true",
        "b,      true",
        "aa,     true",
        "bb,     true",
        "ab,     true",
        "ba,     true",
        "bababa, true",
        "c,      false",
        "ac,     false",
        "bc,     false"
    })
    void testGroupingKleene(String input, boolean expected) {
        runTest("(a|b)*", input, expected, 'a', 'b');
    }

    @ParameterizedTest
    @CsvSource({
        "abd,    true",
        "acd,    true",
        "ad,     false",
        "abcd,   false",
        "'abd d',false"
    })
    void testConcatWithGroupUnion(String input, boolean expected) {
        runTest("a(b|c)d", input, expected, 'a', 'b', 'c', 'd');
    }

    @ParameterizedTest
    @CsvSource({
        "ad,     true",
        "abd,    true",
        "abbbd,  true",
        "acd,    true",
        "acccd,  true",
        "'a c d',false",
        "abcd,   false",
        "abbc,   false"
    })
    void testComplexNesting(String input, boolean expected) {
        runTest("a(b*|c+)?d", input, expected, 'a', 'b', 'c', 'd');
    }

    @ParameterizedTest
    @CsvSource({
        "'',     true",
        "a,      true",
        "aa,     true",
        "b,      false"
    })
    void testNestedKleene(String input, boolean expected) {
        runTest("(a*)*", input, expected, 'a');
    }

    @ParameterizedTest
    @CsvSource({
        "ac,     true",
        "abc,    true",
        "a,      false",
        "c,      false",
        "ab,     false",
        "bc,     false"
    })
    void testOptionalInsideConcat(String input, boolean expected) {
        runTest("a(b?)c", input, expected, 'a', 'b', 'c');
    }

    @ParameterizedTest
    @CsvSource({
        "a,      true",
        "aa,     true",
        "ab,     true",
        "ba,     true",
        "bab,    true",
        "bbaabb, true",
        "'',     false",
        "b,      false",
        "bb,     false",
        "bbbb,   false"
    })
    void testContainsAtLeastOneA(String input, boolean expected) {
        runTest("(a|b)*a(a|b)*", input, expected, 'a', 'b');
    }
}
