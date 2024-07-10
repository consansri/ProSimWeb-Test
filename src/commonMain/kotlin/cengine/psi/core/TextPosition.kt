package cengine.psi.core

/**
 * Represents a position by line and column in a text.
 */
data class TextPosition(val index: Int, val line: Int, val col: Int)