package com.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.DfaSimulator;



import com.compiler.lexer.NfaSimulator;
import com.compiler.lexer.NfaToDfaConverter;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.regex.RegexParser;

public class RegexTest {

    private void runTest(String regex, String input, boolean expected, char... symbols) {
        // Parse regex -> NFA
        RegexParser parser = new RegexParser();
        NFA nfa = parser.parse(regex);
        nfa.getEndState().setFinal(true);


        // Simulación NFA
        NfaSimulator nfaSimulator = new NfaSimulator();
        boolean actualNfa = nfaSimulator.simulate(nfa, input);

        // Construir alfabeto
        Set<Character> alphabet = new HashSet<>();
        for (char c : symbols) alphabet.add(c);

        // Convertir NFA a DFA
        DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
        DfaSimulator dfaSimulator = new DfaSimulator();
        boolean actualDfa = dfaSimulator.simulate(dfa, input);

        // Imprimir detalles
        System.out.println("======================================");
        System.out.println("Regex: '" + regex + "'");
        System.out.println("Input: '" + input + "'");
        System.out.println("Esperado: " + expected);
        System.out.println("Resultado NFA: " + actualNfa);
        System.out.println("Resultado DFA: " + actualDfa);
        System.out.println("DFA States: " + dfa.getStates().size());
        System.out.println("DFA Transitions:");
        dfa.printTransitions(); // Asegúrate de tener este método en tu clase DFA
        System.out.println("======================================\n");

        // Comprobaciones
        assertEquals(expected, actualNfa, "NFA fallo para la cadena: '" + input + "'");
        assertEquals(expected, actualDfa, "DFA fallo para la cadena: '" + input + "'");
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
