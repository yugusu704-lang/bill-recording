package com.localbill.recording

import com.localbill.recording.ui.components.evaluateExpression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorEvaluationTest {

    @Test
    fun testSimpleAddition() {
        val result = evaluateExpression("12+3.5")
        assertEquals(15.5, result!!, 0.001)
    }

    @Test
    fun testSimpleSubtraction() {
        val result = evaluateExpression("100-25.5")
        assertEquals(74.5, result!!, 0.001)
    }

    @Test
    fun testChainedMath() {
        val result = evaluateExpression("10+20.5-5.2")
        assertEquals(25.3, result!!, 0.001)
    }

    @Test
    fun testTrailingOperator() {
        val result = evaluateExpression("15.8+")
        assertEquals(15.8, result!!, 0.001)
    }

    @Test
    fun testInvalidExpression() {
        val result = evaluateExpression("")
        assertNull(result)
    }
}
