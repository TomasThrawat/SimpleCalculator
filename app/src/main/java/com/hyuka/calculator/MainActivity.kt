package com.hyuka.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = Color.Black)) {
                Surface(color = Color.Black) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        CalculatorScreen()
                    }
                }
            }
        }
    }
}

private enum class Op { ADD, SUB, MUL, DIV, NONE }

private fun opSymbol(op: Op): String = when (op) {
    Op.ADD -> "+"
    Op.SUB -> "−"
    Op.MUL -> "×"
    Op.DIV -> "÷"
    Op.NONE -> ""
}

private fun applyOp(a: Double, b: Double, op: Op): Double = when (op) {
    Op.ADD -> a + b
    Op.SUB -> a - b
    Op.MUL -> a * b
    Op.DIV -> if (b != 0.0) a / b else Double.NaN
    Op.NONE -> b
}

@Composable
fun CalculatorScreen() {
    var expression by remember { mutableStateOf("") }
    var currentNumber by remember { mutableStateOf("0") }
    var terms by remember { mutableStateOf(listOf<Pair<String, Op>>()) }
    var newInput by remember { mutableStateOf(true) }
    var justEvaluated by remember { mutableStateOf(false) }

    fun formatResult(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()

    fun rebuildExpression() {
        expression = terms.joinToString(" ") { (num, op) -> "$num ${opSymbol(op)}" }
    }

    fun startFreshIfNeeded() {
        if (justEvaluated) {
            terms = listOf()
            expression = ""
            justEvaluated = false
        }
    }

    fun onDigit(d: String) {
        startFreshIfNeeded()
        currentNumber = if (newInput || currentNumber == "0") d else currentNumber + d
        newInput = false
    }

    fun onDot() {
        startFreshIfNeeded()
        if (newInput) {
            currentNumber = "0."
            newInput = false
        } else if (!currentNumber.contains(".")) {
            currentNumber += "."
        }
    }

    fun onOperator(op: Op) {
        justEvaluated = false
        terms = terms + (currentNumber to op)
        rebuildExpression()
        newInput = true
    }

    fun onEquals() {
        if (terms.isEmpty()) return
        val allTerms = terms + (currentNumber to Op.NONE)
        var acc = allTerms[0].first.toDoubleOrNull() ?: 0.0
        for (i in 1 until allTerms.size) {
            val prevOp = allTerms[i - 1].second
            val nextVal = allTerms[i].first.toDoubleOrNull() ?: 0.0
            acc = applyOp(acc, nextVal, prevOp)
        }
        expression = terms.joinToString(" ") { (num, op) -> "$num ${opSymbol(op)}" } + " $currentNumber ="
        currentNumber = if (acc.isNaN()) "NaN" else formatResult(acc)
        terms = listOf()
        newInput = true
        justEvaluated = true
    }

    fun onClear() {
        currentNumber = "0"
        expression = ""
        terms = listOf()
        newInput = true
        justEvaluated = false
    }

    fun onSign() {
        val current = currentNumber.toDoubleOrNull() ?: 0.0
        currentNumber = formatResult(current * -1)
    }

    fun onPercent() {
        val current = currentNumber.toDoubleOrNull() ?: 0.0
        currentNumber = formatResult(current / 100)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            text = expression,
            color = Color(0xFFAAAAAA),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )

        Text(
            text = currentNumber,
            color = Color.White,
            fontSize = 80.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        val rows = listOf(
            listOf("C" to { onClear() }, "±" to { onSign() }, "%" to { onPercent() }, "÷" to { onOperator(Op.DIV) }),
            listOf("7" to { onDigit("7") }, "8" to { onDigit("8") }, "9" to { onDigit("9") }, "×" to { onOperator(Op.MUL) }),
            listOf("4" to { onDigit("4") }, "5" to { onDigit("5") }, "6" to { onDigit("6") }, "−" to { onOperator(Op.SUB) }),
            listOf("1" to { onDigit("1") }, "2" to { onDigit("2") }, "3" to { onDigit("3") }, "+" to { onOperator(Op.ADD) }),
            listOf("0" to { onDigit("0") }, "." to { onDot() }, "=" to { onEquals() })
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (label, action) ->
                    val isOp = label in listOf("÷", "×", "−", "+", "=")
                    val isTop = label in listOf("C", "±", "%")
                    Button(
                        onClick = action,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isOp -> Color(0xFFFF9500)
                                isTop -> Color(0xFF444444)
                                else -> Color(0xFF1C1C1C)
                            },
                            contentColor = if (isTop) Color.Black else Color.White
                        ),
                        modifier = Modifier
                            .weight(if (label == "0") 2f else 1f)
                            .aspectRatio(if (label == "0") 2f else 1f)
                    ) {
                        Text(text = label, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}
