package com.compiler.lexer;

/**
 * Represents a lexical token rule used by a tokenizer.
 * Each rule defines a token type and the regular expression pattern
 * that matches that type in the input.
 */
public class TokenRule {

    /** The type of token (e.g., IDENTIFIER, NUMBER, KEYWORD) */
    private final String tokenType;

    /** The regular expression used to recognize this token type */
    private final String regex;

    /**
     * Constructs a TokenRule with the specified token type and regex.
     *
     * @param tokenType The type of token this rule represents.
     * @param regex     The regular expression that matches this token.
     */
    public TokenRule(String tokenType, String regex) {
        this.tokenType = tokenType;
        this.regex = regex;
    }

    /**
     * Returns the token type of this rule.
     *
     * @return The token type as a string.
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Returns the regular expression pattern of this rule.
     *
     * @return The regex string.
     */
    public String getRegex() {
        return regex;
    }
}
