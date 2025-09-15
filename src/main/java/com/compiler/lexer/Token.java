package com.compiler.lexer;

/**
 * Represents a lexical token with a type and its corresponding lexeme.
 * A token is the fundamental unit recognized by a lexer or scanner.
 */
public class Token {

    /** The type or category of the token (e.g., IDENTIFIER, NUMBER, KEYWORD) */
    private final String type;

    /** The actual string value of the token from the source input */
    private final String lexeme;

    /**
     * Constructs a Token with the specified type and lexeme.
     *
     * @param type   The type of the token.
     * @param lexeme The string value of the token.
     */
    public Token(String type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }

    /**
     * Returns the type of the token.
     *
     * @return The token's type as a string.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the lexeme of the token.
     *
     * @return The actual string value of the token.
     */
    public String getLexeme() {
        return lexeme;
    }

    /**
     * Returns a string representation of the token in the format:
     * Token(type='TYPE', lexeme='LEXEME')
     *
     * @return The string representation of the token.
     */
    @Override
    public String toString() {
        return String.format("Token(type='%s', lexeme='%s')", type, lexeme);
    }
}
