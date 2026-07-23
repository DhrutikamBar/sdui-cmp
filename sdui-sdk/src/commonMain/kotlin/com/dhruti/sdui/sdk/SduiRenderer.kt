package com.dhruti.sdui.sdk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.sdui.shared.UiNode

@Composable
fun SduiRenderer(
    screen: UiNode,
    actionHandler: ActionHandler,
    modifier: Modifier = Modifier,
    registry: ComponentRegistry = ComponentRegistry().apply { registerCoreWidgets() },
    formState: FormState = rememberSaveable(saver = FormState.Saver) { FormState() }
) {
    registry.RenderRoot(screen, actionHandler, formState)
}
