package com.university.scientificcalculator.calculator

import com.university.scientificcalculator.model.AngleMode
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Outcome of evaluating an expression. Kept separate from exceptions so the
 * UI layer never has to deal with try/catch or crashes on bad input.
 */
sealed class EngineResult {
    data class Success(val value: Double) : EngineResult()
    data class Error(val message: String) : EngineResult()
}

/** Internal exception used to unwind the recursive-descent parser on bad input. */
private class CalculatorException(message: String) : Exception(message)

/**
 * A minimal set of tokens produced by [Lexer] and consumed by [Parser].
 */
private sealed class Token {
    data class Number(val value: Double) : Token()
    data class Function(val name: String) : Token()
    data class Constant(val name: String) : Token()
    object Plus : Token()
    object Minus : Token()
    object Multiply : Token()
    object Divide : Token()
    object Power : Token()
    object Percent : Token()
    object Factorial : Token()
    object LParen : Token()
    object RParen : Token()
    object End : Token()
}

/** Turns a raw expression string (as typed on the keypad) into a list of [Token]s. */
private class Lexer(private val source: String) {

    private val functionNames = listOf(
        "asin", "acos", "atan", "sin", "cos", "tan", "log", "ln", "sqrt", "abs"
    )

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < source.length) {
            val c = source[i]
            when {
                c.isWhitespace() -> i++

                c.isDigit() || c == '.' -> {
                    val start = i
                    var dotSeen = false
                    while (i < source.length && (source[i].isDigit() || source[i] == '.')) {
                        if (source[i] == '.') {
                            if (dotSeen) throw CalculatorException("Invalid Expression")
                            dotSeen = true
                        }
                        i++
                    }
                    val text = source.substring(start, i)
                    val value = text.toDoubleOrNull()
                        ?: throw CalculatorException("Invalid Expression")
                    tokens.add(Token.Number(value))
                }

                c.isLetter() -> {
                    val start = i
                    while (i < source.length && source[i].isLetter()) i++
                    val word = source.substring(start, i)
                    when (word) {
                        in functionNames -> tokens.add(Token.Function(word))
                        "pi" -> tokens.add(Token.Constant("pi"))
                        "e" -> tokens.add(Token.Constant("e"))
                        else -> throw CalculatorException("Invalid Expression")
                    }
                }

                c == 'π' -> {
                    tokens.add(Token.Constant("pi")); i++
                }

                c == '√' -> {
                    tokens.add(Token.Function("sqrt")); i++
                }

                c == '+' -> {
                    tokens.add(Token.Plus); i++
                }

                c == '-' || c == '−' -> {
                    tokens.add(Token.Minus); i++
                }

                c == '×' || c == '*' -> {
                    tokens.add(Token.Multiply); i++
                }

                c == '÷' || c == '/' -> {
                    tokens.add(Token.Divide); i++
                }

                c == '^' -> {
                    tokens.add(Token.Power); i++
                }

                c == '%' -> {
                    tokens.add(Token.Percent); i++
                }

                c == '!' -> {
                    tokens.add(Token.Factorial); i++
                }

                c == '(' -> {
                    tokens.add(Token.LParen); i++
                }

                c == ')' -> {
                    tokens.add(Token.RParen); i++
                }

                else -> throw CalculatorException("Invalid Expression")
            }
        }
        tokens.add(Token.End)
        return tokens
    }
}

/**
 * Recursive-descent parser + evaluator, combined into a single pass since the
 * calculator never needs to reuse a parsed tree (each keystroke re-evaluates).
 *
 * Grammar (highest precedence last):
 *   expression := term (('+' | '-') term)*
 *   term       := unary (explicitOp unary | implicitMultiply)*
 *   unary      := ('-' | '+')? power
 *   power      := postfix ('^' unary)?           // right-associative
 *   postfix    := primary ('!' | '%')*
 *   primary    := NUMBER | CONSTANT | FUNCTION '(' expression ')' | '(' expression ')'
 */
private class Parser(private val tokens: List<Token>, private val angleMode: AngleMode) {

    private var pos = 0
    private fun peek(): Token = tokens[pos]
    private fun advance(): Token = tokens[pos].also { pos++ }

    fun parseFully(): Double {
        val result = parseExpression()
        if (peek() != Token.End) throw CalculatorException("Invalid Expression")
        return result
    }

    private fun parseExpression(): Double {
        var value = parseTerm()
        while (true) {
            when (peek()) {
                Token.Plus -> {
                    advance(); value += parseTerm()
                }
                Token.Minus -> {
                    advance(); value -= parseTerm()
                }
                else -> return value
            }
        }
    }

    private fun startsPrimary(token: Token): Boolean = when (token) {
        is Token.Number, is Token.Constant, is Token.Function, Token.LParen -> true
        else -> false
    }

    private fun parseTerm(): Double {
        var value = parseUnary()
        while (true) {
            when (peek()) {
                Token.Multiply -> {
                    advance(); value *= parseUnary()
                }
                Token.Divide -> {
                    advance()
                    val divisor = parseUnary()
                    if (divisor == 0.0) throw CalculatorException("Math Error")
                    value /= divisor
                }
                else -> {
                    // Implicit multiplication, e.g. "2π", "2(3+4)", "sin(30)cos(30)".
                    if (startsPrimary(peek())) {
                        value *= parseUnary()
                    } else {
                        return value
                    }
                }
            }
        }
    }

    private fun parseUnary(): Double {
        return when (peek()) {
            Token.Minus -> {
                advance(); -parseUnary()
            }
            Token.Plus -> {
                advance(); parseUnary()
            }
            else -> parsePower()
        }
    }

    private fun parsePower(): Double {
        val base = parsePostfix()
        if (peek() == Token.Power) {
            advance()
            val exponent = parseUnary()
            val result = base.pow(exponent)
            if (result.isNaN()) throw CalculatorException("Math Error")
            return result
        }
        return base
    }

    private fun parsePostfix(): Double {
        var value = parsePrimary()
        while (true) {
            when (peek()) {
                Token.Factorial -> {
                    advance()
                    value = factorial(value)
                }
                Token.Percent -> {
                    advance()
                    value /= 100.0
                }
                else -> return value
            }
        }
    }

    private fun parsePrimary(): Double {
        return when (val token = advance()) {
            is Token.Number -> token.value

            is Token.Constant -> when (token.name) {
                "pi" -> PI
                "e" -> Math.E
                else -> throw CalculatorException("Invalid Expression")
            }

            is Token.Function -> {
                // Prefer "sin(30)" style, but also accept a bare argument like
                // "√25" (no parentheses) since that is common calculator shorthand.
                val argument = if (peek() == Token.LParen) {
                    advance()
                    val inner = parseExpression()
                    if (advance() != Token.RParen) throw CalculatorException("Invalid Expression")
                    inner
                } else {
                    parseUnary()
                }
                applyFunction(token.name, argument)
            }

            Token.LParen -> {
                val value = parseExpression()
                if (advance() != Token.RParen) throw CalculatorException("Invalid Expression")
                value
            }

            else -> throw CalculatorException("Invalid Expression")
        }
    }

    private fun toRadians(value: Double): Double =
        if (angleMode == AngleMode.DEG) Math.toRadians(value) else value

    private fun fromRadians(value: Double): Double =
        if (angleMode == AngleMode.DEG) Math.toDegrees(value) else value

    private fun applyFunction(name: String, argument: Double): Double {
        return when (name) {
            "sin" -> sin(toRadians(argument))
            "cos" -> cos(toRadians(argument))
            "tan" -> {
                val radians = toRadians(argument)
                val cosValue = cos(radians)
                if (abs(cosValue) < 1e-12) throw CalculatorException("Math Error")
                tan(radians)
            }
            "asin" -> {
                if (argument < -1.0 || argument > 1.0) throw CalculatorException("Math Error")
                fromRadians(asin(argument))
            }
            "acos" -> {
                if (argument < -1.0 || argument > 1.0) throw CalculatorException("Math Error")
                fromRadians(acos(argument))
            }
            "atan" -> fromRadians(atan(argument))
            "log" -> {
                if (argument <= 0.0) throw CalculatorException("Math Error")
                log10(argument)
            }
            "ln" -> {
                if (argument <= 0.0) throw CalculatorException("Math Error")
                ln(argument)
            }
            "sqrt" -> {
                if (argument < 0.0) throw CalculatorException("Math Error")
                sqrt(argument)
            }
            "abs" -> abs(argument)
            else -> throw CalculatorException("Invalid Expression")
        }
    }

    private fun factorial(value: Double): Double {
        if (value < 0.0 || abs(value - value.roundToLong()) > 1e-9) {
            throw CalculatorException("Math Error")
        }
        val n = value.roundToLong()
        if (n > 170) throw CalculatorException("Math Error") // overflows Double beyond this
        var result = 1.0
        for (i in 2..n) result *= i
        return result
    }
}

/**
 * Public entry point for the calculation engine. Stateless and UI-agnostic so
 * it can be unit tested in isolation from Compose / Android.
 */
object CalculatorEngine {

    fun evaluate(expression: String, angleMode: AngleMode): EngineResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return EngineResult.Error("Invalid Expression")

        return try {
            val openParens = trimmed.count { it == '(' }
            val closeParens = trimmed.count { it == ')' }
            if (openParens != closeParens) throw CalculatorException("Invalid Expression")

            val tokens = Lexer(trimmed).tokenize()
            val result = Parser(tokens, angleMode).parseFully()

            if (result.isNaN() || result.isInfinite()) {
                EngineResult.Error("Math Error")
            } else {
                EngineResult.Success(result)
            }
        } catch (e: CalculatorException) {
            EngineResult.Error(e.message ?: "Invalid Expression")
        } catch (e: Exception) {
            // Safety net: the app must never crash regardless of input.
            EngineResult.Error("Invalid Expression")
        }
    }

    /** Formats a raw double the way a calculator display should. */
    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Math Error"

        val rounded = kotlin.math.round(value * 1e10) / 1e10

        return if (abs(rounded) in 1e-9..1e-4 && rounded != 0.0 || abs(rounded) >= 1e15) {
            // Extremely small or large: use scientific notation for readability.
            String.format(java.util.Locale.US, "%.6E", rounded)
        } else if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString().trimEnd('0').trimEnd('.')
        }
    }
}
