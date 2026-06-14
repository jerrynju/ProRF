package com.prorf.dsl

internal enum class TokenType {
    IDENT,
    STRING,
    NUMBER,
    LBRACE,
    RBRACE,
    COLON,
    DOT,
    EQUALS,
    ARROW,
    SYMBOL,
    NEWLINE,
    EOF,
}

internal data class Token(
    val type: TokenType,
    val text: String,
    val position: Int,
)

internal class DslLexer(private val source: String) {
    private var index = 0

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (index < source.length) {
            val c = source[index]
            when {
                c == ' ' || c == '\t' || c == '\r' -> index++
                c == '\n' || c == ';' -> {
                    tokens += Token(TokenType.NEWLINE, c.toString(), index)
                    index++
                }
                c == '/' && peek(1) == '/' -> skipLineComment()
                c == '{' -> tokens += single(TokenType.LBRACE)
                c == '}' -> tokens += single(TokenType.RBRACE)
                c == ':' -> tokens += single(TokenType.COLON)
                c == '.' -> tokens += single(TokenType.DOT)
                c == '=' -> tokens += single(TokenType.EQUALS)
                c == '-' && peek(1) == '>' -> {
                    tokens += Token(TokenType.ARROW, "->", index)
                    index += 2
                }
                c == '"' -> tokens += readString()
                c.isDigit() || ((c == '-' || c == '+') && peek(1)?.isDigit() == true) -> tokens += readNumber()
                c in setOf('+', '-', '*', '/', '(', ')', '[', ']', ',') -> tokens += single(TokenType.SYMBOL)
                isIdentStart(c) -> tokens += readIdent()
                else -> error("Unexpected character '$c' at offset $index")
            }
        }
        tokens += Token(TokenType.EOF, "", source.length)
        return tokens
    }

    private fun single(type: TokenType): Token = Token(type, source[index].toString(), index).also { index++ }

    private fun peek(offset: Int): Char? = source.getOrNull(index + offset)

    private fun skipLineComment() {
        while (index < source.length && source[index] != '\n') index++
    }

    private fun readString(): Token {
        val start = index
        index++
        val text = StringBuilder()
        while (index < source.length && source[index] != '"') {
            text.append(source[index])
            index++
        }
        require(index < source.length) { "Unterminated string at offset $start" }
        index++
        return Token(TokenType.STRING, text.toString(), start)
    }

    private fun readNumber(): Token {
        val start = index
        if (source[index] == '-' || source[index] == '+') index++
        while (index < source.length && source[index].isDigit()) index++
        if (source.getOrNull(index) == '.') {
            index++
            while (index < source.length && source[index].isDigit()) index++
        }
        return Token(TokenType.NUMBER, source.substring(start, index), start)
    }

    private fun readIdent(): Token {
        val start = index
        while (index < source.length && isIdentPart(source[index])) index++
        return Token(TokenType.IDENT, source.substring(start, index), start)
    }

    private fun isIdentStart(c: Char): Boolean = c.isLetter() || c == '_'

    private fun isIdentPart(c: Char): Boolean =
        c.isLetterOrDigit() || c == '_' || c == '-' || c == '/' || c == '@'
}
