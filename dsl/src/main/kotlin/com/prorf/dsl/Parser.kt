package com.prorf.dsl

internal class DslParser(source: String) {
    private val tokens = DslLexer(source).tokenize()
    private var index = 0

    fun parse(): WorkflowDocumentAst {
        skipNewlines()
        expectKeyword("workflow")
        val name = expect(TokenType.STRING, "workflow name").text
        expect(TokenType.LBRACE, "'{' after workflow name")

        var imports = emptyList<String>()
        var variables = emptyList<DslAssignment>()
        var nodes = emptyList<DslNode>()
        var edges = emptyList<DslEdge>()
        var scenarios = emptyList<DslScenario>()
        var outputs = emptyList<DslReference>()

        while (!check(TokenType.RBRACE)) {
            skipNewlines()
            if (check(TokenType.RBRACE)) break
            when (val section = expect(TokenType.IDENT, "workflow section").text) {
                "imports" -> imports = parseImports()
                "variables" -> variables = parseAssignmentsBlock()
                "nodes" -> nodes = parseNodes()
                "edges" -> edges = parseEdges()
                "scenarios" -> scenarios = parseScenarios()
                "outputs" -> outputs = parseOutputs()
                else -> errorAtCurrent("Unknown workflow section '$section'")
            }
        }
        expect(TokenType.RBRACE, "'}' after workflow")
        skipNewlines()
        expect(TokenType.EOF, "end of file")
        return WorkflowDocumentAst(name, imports, variables, nodes, edges, scenarios, outputs)
    }

    private fun parseImports(): List<String> = parseBlockItems {
        parseReference().toString()
    }

    private fun parseAssignmentsBlock(): List<DslAssignment> = parseBlockItems {
        parseAssignment()
    }

    private fun parseNodes(): List<DslNode> = parseBlockItems {
        val id = expect(TokenType.IDENT, "node id").text
        expect(TokenType.COLON, "':' after node id")
        val type = expect(TokenType.IDENT, "node type").text
        val parameters = parseAssignmentsBlock()
        DslNode(id, type, parameters)
    }

    private fun parseEdges(): List<DslEdge> = parseBlockItems {
        val from = parseReference()
        expect(TokenType.ARROW, "'->' in edge")
        val to = parseReference()
        DslEdge(from, to)
    }

    private fun parseScenarios(): List<DslScenario> = parseBlockItems {
        val name = expect(TokenType.IDENT, "scenario name").text
        if (check(TokenType.LBRACE)) {
            DslScenario(name, parseAssignmentsBlock())
        } else {
            DslScenario(name)
        }
    }

    private fun parseOutputs(): List<DslReference> = parseBlockItems {
        parseReference()
    }

    private fun parseAssignment(): DslAssignment {
        val path = parseReference()
        expect(TokenType.EQUALS, "'=' in assignment")
        return DslAssignment(path, parseExpressionUntilBoundary())
    }

    private fun parseReference(): DslReference {
        val parts = mutableListOf(parseReferencePart())
        while (match(TokenType.DOT)) {
            parts += parseReferencePart()
        }
        return DslReference(parts)
    }

    private fun parseReferencePart(): String {
        val base = expect(TokenType.IDENT, "identifier").text
        if (current().type == TokenType.SYMBOL && current().text == "[") {
            advance()
            val index = expect(TokenType.NUMBER, "port index").text
            val close = expect(TokenType.SYMBOL, "']'")
            if (close.text != "]") errorAt(close, "Expected ']' after port index")
            return "$base[$index]"
        }
        return base
    }

    private fun parseExpressionUntilBoundary(): DslExpression {
        val parts = mutableListOf<Token>()
        while (!check(TokenType.NEWLINE) && !check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            parts += advance()
        }
        return DslExpression(renderExpression(parts))
    }

    private fun renderExpression(tokens: List<Token>): String {
        val compactBefore = setOf(".", ")", "]", ",")
        val compactAfter = setOf(".", "(", "[")
        return buildString {
            tokens.forEachIndexed { i, token ->
                val previousToken = tokens.getOrNull(i - 1)
                val previous = previousToken?.text
                val current = token.text
                val functionCall = current == "(" && previousToken?.type == TokenType.IDENT
                val needsSpace = i > 0 && !functionCall && previous !in compactAfter && current !in compactBefore
                if (needsSpace) append(' ')
                append(current)
            }
        }.trim()
    }

    private inline fun <T> parseBlockItems(parseItem: () -> T): List<T> {
        expect(TokenType.LBRACE, "'{'")
        val items = mutableListOf<T>()
        while (!check(TokenType.RBRACE)) {
            skipNewlines()
            if (!check(TokenType.RBRACE)) {
                items += parseItem()
            }
            skipNewlines()
        }
        expect(TokenType.RBRACE, "'}'")
        return items
    }

    private fun expectKeyword(keyword: String) {
        val token = expect(TokenType.IDENT, keyword)
        if (token.text != keyword) errorAt(token, "Expected '$keyword' but found '${token.text}'")
    }

    private fun expect(type: TokenType, expected: String): Token {
        if (!check(type)) errorAtCurrent("Expected $expected but found '${current().text}'")
        return advance()
    }

    private fun match(type: TokenType): Boolean {
        if (!check(type)) return false
        advance()
        return true
    }

    private fun check(type: TokenType): Boolean = current().type == type

    private fun skipNewlines() {
        while (match(TokenType.NEWLINE)) Unit
    }

    private fun advance(): Token = tokens[index++]

    private fun current(): Token = tokens[index]

    private fun errorAtCurrent(message: String): Nothing = errorAt(current(), message)

    private fun errorAt(token: Token, message: String): Nothing =
        throw IllegalArgumentException("$message at offset ${token.position}")
}
