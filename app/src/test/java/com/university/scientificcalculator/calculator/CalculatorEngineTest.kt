package com.university.scientificcalculator.calculator

import com.university.scientificcalculator.model.AngleMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    private fun evalDeg(expr: String): Double {
        val result = CalculatorEngine.evaluate(expr, AngleMode.DEG)
        assertTrue("Expected success for '$expr' but got $result", result is EngineResult.Success)
        return (result as EngineResult.Success).value
    }

    private fun evalRad(expr: String): Double {
        val result = CalculatorEngine.evaluate(expr, AngleMode.RAD)
        assertTrue("Expected success for '$expr' but got $result", result is EngineResult.Success)
        return (result as EngineResult.Success).value
    }

    private val EPSILON = 1e-6

    // --- Basic operations -------------------------------------------------

    @Test
    fun addition() {
        assertEquals(5.0, evalDeg("2 + 3"), EPSILON)
    }

    @Test
    fun subtraction() {
        assertEquals(6.0, evalDeg("10 - 4"), EPSILON)
    }

    @Test
    fun multiplication() {
        assertEquals(30.0, evalDeg("5 × 6"), EPSILON)
    }

    @Test
    fun division() {
        assertEquals(5.0, evalDeg("20 ÷ 4"), EPSILON)
    }

    @Test
    fun decimalNumbers() {
        assertEquals(3.3, evalDeg("1.1 + 2.2"), EPSILON)
    }

    @Test
    fun negativeNumbers() {
        assertEquals(-5.0, evalDeg("-2 - 3"), EPSILON)
        assertEquals(1.0, evalDeg("-2 + 3"), EPSILON)
    }

    @Test
    fun percentage() {
        assertEquals(0.5, evalDeg("50%"), EPSILON)
    }

    // --- Precedence and parentheses ----------------------------------------

    @Test
    fun operatorPrecedence() {
        assertEquals(14.0, evalDeg("2 + 3 × 4"), EPSILON)
    }

    @Test
    fun parentheses() {
        assertEquals(20.0, evalDeg("(2 + 3) × 4"), EPSILON)
    }

    @Test
    fun nestedParentheses() {
        assertEquals(21.0, evalDeg("3 × (2 + (1 + 4))"), EPSILON)
    }

    // --- Powers and roots ---------------------------------------------------

    @Test
    fun squareRoot() {
        assertEquals(5.0, evalDeg("√25"), EPSILON)
        assertEquals(5.0, evalDeg("√(25)"), EPSILON)
    }

    @Test
    fun square() {
        assertEquals(25.0, evalDeg("5^2"), EPSILON)
    }

    @Test
    fun power() {
        assertEquals(8.0, evalDeg("2^3"), EPSILON)
    }

    // --- Trigonometric functions --------------------------------------------

    @Test
    fun sineDegrees() {
        assertEquals(0.5, evalDeg("sin(30)"), 1e-4)
    }

    @Test
    fun cosineDegrees() {
        assertEquals(0.5, evalDeg("cos(60)"), 1e-4)
    }

    @Test
    fun tangentDegrees() {
        assertEquals(1.0, evalDeg("tan(45)"), 1e-4)
    }

    @Test
    fun sineRadians() {
        assertEquals(1.0, evalRad("sin(π/2)"), 1e-4)
    }

    @Test
    fun inverseTrig() {
        assertEquals(30.0, evalDeg("asin(0.5)"), 1e-3)
    }

    // --- Logarithms -----------------------------------------------------

    @Test
    fun log10() {
        assertEquals(2.0, evalDeg("log(100)"), EPSILON)
    }

    @Test
    fun naturalLog() {
        assertEquals(1.0, evalDeg("ln(e)"), EPSILON)
    }

    // --- Constants ---------------------------------------------------------

    @Test
    fun constantsAndImplicitMultiplication() {
        assertEquals(2 * Math.PI, evalDeg("2π"), EPSILON)
        assertEquals(2 * Math.PI, evalDeg("2 × π"), EPSILON)
    }

    // --- Factorial -----------------------------------------------------

    @Test
    fun factorial() {
        assertEquals(120.0, evalDeg("5!"), EPSILON)
    }

    @Test
    fun factorialOfNegativeIsError() {
        val result = CalculatorEngine.evaluate("(-1)!", AngleMode.DEG)
        assertTrue(result is EngineResult.Error)
    }

    // --- DEG / RAD mode ------------------------------------------------

    @Test
    fun degModeDiffersFromRadMode() {
        val degResult = evalDeg("sin(90)")
        val radResult = evalRad("sin(90)")
        assertTrue(Math.abs(degResult - radResult) > 0.1)
    }

    // --- Error handling --------------------------------------------------

    @Test
    fun divisionByZeroIsError() {
        val result = CalculatorEngine.evaluate("5 ÷ 0", AngleMode.DEG)
        assertTrue(result is EngineResult.Error)
    }

    @Test
    fun invalidExpressionIsError() {
        val result = CalculatorEngine.evaluate("2 + × 3", AngleMode.DEG)
        assertTrue(result is EngineResult.Error)
    }

    @Test
    fun mismatchedParenthesesIsError() {
        val result = CalculatorEngine.evaluate("(2 + 3", AngleMode.DEG)
        assertTrue(result is EngineResult.Error)
    }

    @Test
    fun negativeSquareRootIsError() {
        val result = CalculatorEngine.evaluate("√(-4)", AngleMode.DEG)
        assertTrue(result is EngineResult.Error)
    }

    @Test
    fun logOfNonPositiveIsError() {
        val result = CalculatorEngine.evaluate("log(-5)", AngleMode.DEG)
        assertTrue(result is EngineResult.Error)
    }

    @Test
    fun emptyExpressionIsError() {
        val result = CalculatorEngine.evaluate("", AngleMode.DEG)
        assertTrue(result is EngineResult.Error)
    }
}
