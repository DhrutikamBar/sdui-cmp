package com.dhruti.sdui.sdk

import com.example.sdui.shared.SduiValue

/**
 * Small, bounded expression parser for legacy `Condition.Script` rules.
 *
 * It intentionally supports only values, multiplication, and one comparison:
 * `amount * quantity >= 100`, `status == 'active'`, and `enabled == true`.
 * It does not evaluate functions, member calls, assignments, or arbitrary code.
 */
internal object SduiExpressionEvaluator {
    fun evaluate(expression: String, state: FormState): Boolean = try {
        val parser = Parser(expression, state)
        val left = parser.product()
        val operator = parser.comparisonOperator() ?: return false
        val right = parser.product()
        parser.requireEnd()
        compare(left, operator, right)
    } catch (_: IllegalArgumentException) {
        false
    }

    private fun compare(left: Any, operator: String, right: Any): Boolean {
        if (operator == "==") return left == right
        val leftNumber = left as? Double ?: return false
        val rightNumber = right as? Double ?: return false
        return when (operator) {
            ">" -> leftNumber > rightNumber
            "<" -> leftNumber < rightNumber
            ">=" -> leftNumber >= rightNumber
            "<=" -> leftNumber <= rightNumber
            else -> false
        }
    }

    private class Parser(private val input: String, private val state: FormState) {
        private var index = 0

        fun product(): Any {
            var value = value()
            while (consume("*")) {
                val left = value as? Double ?: throw IllegalArgumentException("Non-numeric product")
                val right = value() as? Double ?: throw IllegalArgumentException("Non-numeric product")
                value = left * right
            }
            return value
        }

        fun comparisonOperator(): String? {
            skipWhitespace()
            return listOf(">=", "<=", "==", ">", "<").firstOrNull(::consume)
        }

        fun requireEnd() {
            skipWhitespace()
            if (index != input.length) throw IllegalArgumentException("Unexpected expression content")
        }

        private fun value(): Any {
            skipWhitespace()
            if (index >= input.length) throw IllegalArgumentException("Expected value")
            return when (input[index]) {
                '\'' -> quotedString()
                else -> {
                    val token = readToken()
                    when {
                        token == "true" -> true
                        token == "false" -> false
                        token.toDoubleOrNull() != null -> token.toDouble()
                        else -> state[token].toExpressionValue()
                            ?: throw IllegalArgumentException("Unknown identifier")
                    }
                }
            }
        }

        private fun quotedString(): String {
            index++
            val start = index
            while (index < input.length && input[index] != '\'') index++
            if (index >= input.length) throw IllegalArgumentException("Unterminated string")
            return input.substring(start, index++)
        }

        private fun readToken(): String {
            val start = index
            while (index < input.length && !input[index].isWhitespace() && input[index] !in "*<>=\'") index++
            if (start == index) throw IllegalArgumentException("Expected token")
            return input.substring(start, index)
        }

        private fun consume(value: String): Boolean {
            skipWhitespace()
            if (!input.startsWith(value, index)) return false
            index += value.length
            return true
        }

        private fun skipWhitespace() {
            while (index < input.length && input[index].isWhitespace()) index++
        }
    }

    private fun SduiValue?.toExpressionValue(): Any? = when (this) {
        is SduiValue.StringValue -> value
        is SduiValue.NumberValue -> value
        is SduiValue.BooleanValue -> value
        else -> null
    }
}
