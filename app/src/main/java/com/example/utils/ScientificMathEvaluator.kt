package com.example.utils

import java.text.DecimalFormat
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object ScientificMathEvaluator {

    fun evaluate(expression: String, useDegrees: Boolean = true): String {
        val cleaned = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", Math.PI.toString())
            .replace("√(", "sqrt(")
            .replace(" ", "")
        if (cleaned.isEmpty()) return "0"
        return try {
            val value = Parser(cleaned, useDegrees).parse()
            if (value.isNaN() || value.isInfinite()) {
                "Error"
            } else {
                val df = DecimalFormat("#.########")
                val formatted = df.format(value)
                if (formatted == "-0") "0" else formatted
            }
        } catch (_: Exception) {
            "Error"
        }
    }

    private class Parser(
        private val input: String,
        private val useDegrees: Boolean
    ) {
        private var pos = -1
        private var ch = 0

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < input.length) throw IllegalArgumentException("Unexpected char: ${ch.toChar()}")
            return x
        }

        private fun nextChar() {
            ch = if (++pos < input.length) input[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> x /= parseFactor()
                    eat('%'.code) -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                if (!eat(')'.code)) throw IllegalArgumentException("Missing ')'")
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code || ch == 'E'.code) {
                    if (ch == 'E'.code) {
                        nextChar()
                        if (ch == '-'.code || ch == '+'.code) nextChar()
                    } else {
                        nextChar()
                    }
                }
                x = input.substring(startPos, pos).toDouble()
            } else if (ch in 'a'.code..'z'.code) {
                while (ch in 'a'.code..'z'.code) nextChar()
                val name = input.substring(startPos, pos)
                x = when (name) {
                    "e" -> Math.E
                    else -> {
                        val arg = parseFactor()
                        val angle = if (useDegrees) Math.toRadians(arg) else arg
                        when (name) {
                            "sqrt" -> sqrt(arg)
                            "sin" -> sin(angle)
                            "cos" -> cos(angle)
                            "tan" -> tan(angle)
                            "log" -> log10(arg)
                            "ln" -> ln(arg)
                            else -> throw IllegalArgumentException("Unknown function: $name")
                        }
                    }
                }
            } else {
                throw IllegalArgumentException("Unexpected char: ${ch.toChar()}")
            }

            if (eat('^'.code)) {
                x = x.pow(parseFactor())
            }
            return x
        }
    }
}
