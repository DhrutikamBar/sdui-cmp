package com.example.sdui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * The "core" widgets every app gets for free. A real SDK ships a small set like this one;
 * each host app then calls `registry.register(...)` for its own app-specific widgets
 * (a candlestick chart, a product carousel) exactly the same way this function does.
 */
fun ComponentRegistry.registerCoreWidgets() {

    register("column") { node, actions, formState ->
        Column(Modifier.padding(12.dp)) {
            node.children.forEach { child -> Render(child, actions, formState) }
        }
    }

    register("text") { node, _, _ ->
        Text(node.props["value"]?.jsonPrimitive?.contentOrNull ?: "")
    }

    register("textInput") { node, _, formState ->
        val fieldId = node.id ?: ""
        // Reading formState[fieldId] here (backed by mutableStateMapOf) is what makes the
        // button's rule check below react live as the person types — no local state needed.
        OutlinedTextField(
            value = formState[fieldId] ?: "",
            onValueChange = { formState[fieldId] = it },
            label = { Text(node.props["label"]?.jsonPrimitive?.contentOrNull ?: "") }
        )
    }

    register("button") { node, actions, formState ->
        // Enabled only if every rule attached to this node is satisfied — empty rules
        // list means "always enabled". This is the age-field -> submit-button example
        // from PhonePe's LiquidUI, minus the general expression parser.
        val enabled = node.rules.all { it.isSatisfied(formState) }
        Button(onClick = { node.action?.let(actions::handle) }, enabled = enabled) {
            Text(node.props["label"]?.jsonPrimitive?.contentOrNull ?: "")
        }
    }

    register("card") { node, actions, _ ->
        // This is the data-binding pattern from earlier in the conversation: the server
        // already resolved real data (product name/price) into these props before sending it.
        ElevatedCard(
            onClick = { node.action?.let(actions::handle) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    node.props["title"]?.jsonPrimitive?.contentOrNull ?: "",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(node.props["subtitle"]?.jsonPrimitive?.contentOrNull ?: "")
            }
        }
    }
}
