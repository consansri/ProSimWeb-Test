package cengine.lexer.core

/**
 * Represents a severity associated with a token.
 */
data class Severity(val type: Type, val message: String){

    enum class Type{
        ERROR,
        WARNING,
        INFO
    }
}
