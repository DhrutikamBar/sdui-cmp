package com.example.sdui.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiNode

private fun SduiValue?.asString() = (this as? SduiValue.StringValue)?.value ?: ""

@Composable
fun BalanceToggle(node: UiNode) {
    var visible by remember { mutableStateOf(true) }
    val amount = node.props["amount"].asString()
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedContent(targetState = visible, label = "balanceVisibility") { isVisible ->
            Text(
                text = if (isVisible) amount else "••••••",
                fontSize = 32.sp,
                color = Color.White // Fix: Navy background requirement
            )
        }
        Text(
            text = if (visible) " 👁️" else " 🙈",
            modifier = Modifier.clickable { visible = !visible },
            color = Color.White
        )
    }
}
