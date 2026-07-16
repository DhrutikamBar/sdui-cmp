package com.example.sdui.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonPrimitive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.intOrNull
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType


val LocalSnackBarHostState = compositionLocalOf<SnackbarHostState?> { null }
@OptIn(ExperimentalMaterial3Api::class)
fun ComponentRegistry.registerCoreWidgets() {

    register("column") { node, actions, formState ->
        val style = node.style()
        var modifier = Modifier.applyStyle(style)
        if (style.animateSize == true) modifier = modifier.animateContentSize()
        if (style.scrollable == true) modifier = modifier.verticalScroll(rememberScrollState())
        if (node.action != null) modifier = modifier.clickable { node.action?.let(actions::handle) }
        Column(modifier = modifier, horizontalAlignment = parseColumnAlignment(style.alignment)) {
            node.children.forEach { child -> Render(child, actions, formState) }
        }
    }

    register("row") { node, actions, formState ->
        val style = node.style()
        var modifier = Modifier.applyStyle(style)
        if (style.animateSize == true) modifier = modifier.animateContentSize()
        if (style.scrollable == true) modifier = modifier.horizontalScroll(rememberScrollState())
        if (node.action != null) modifier = modifier.clickable { node.action?.let(actions::handle) }
        Row(
            modifier = modifier,
            horizontalArrangement = parseArrangement(style.arrangement),
            verticalAlignment = Alignment.CenterVertically
        ) {
            node.children.forEach { child -> Render(child, actions, formState) }
        }
    }

    register("box") { node, actions, formState ->
        val style = node.style()
        var base = Modifier.applyStyle(style)
        if (style.animateSize == true) base = base.animateContentSize()
        val clickableModifier = if (node.action != null) {
            base.clickable { node.action?.let(actions::handle) }
        } else base
        Box(modifier = clickableModifier, contentAlignment = parseBoxAlignment(style.alignment)) {
            node.children.forEach { child -> Render(child, actions, formState) }
        }
    }

    register("text") { node, _, _ ->
        val style = node.style()
        StyledText(
            value = node.props["value"]?.jsonPrimitive?.contentOrNull ?: "",
            style = style,
            modifier = Modifier.applyStyle(style)
        )
    }

    register("image") { node, actions, _ ->
        val style = node.style()
        val url = node.props["url"]?.jsonPrimitive?.contentOrNull
        val emoji = node.props["icon"]?.jsonPrimitive?.contentOrNull
        val base = Modifier.applyStyle(style)
        val clickableModifier = if (node.action != null) {
            base.clickable { node.action?.let(actions::handle) }
        } else base
        when {
            url != null -> AsyncImage(
                model = url,
                contentDescription = null,
                modifier = clickableModifier,
                contentScale = ContentScale.Crop
            )
            emoji != null -> Box(modifier = clickableModifier, contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = (style.fontSize ?: 20).sp, textAlign = TextAlign.Center)
            }
            else -> {}
        }
    }

    register("icon") { node, actions, _ ->
        val style = node.style()
        val vector = materialIcon(node.props["name"]?.jsonPrimitive?.contentOrNull ?: "")
        val description = node.props["contentDescription"]?.jsonPrimitive?.contentOrNull
        var base = Modifier.applyStyle(style)
        if (node.action != null) base = base.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        val clickableModifier = if (node.action != null) base.clickable { node.action?.let(actions::handle) } else base
        if (vector != null) {
            Icon(imageVector = vector, contentDescription = description, modifier = clickableModifier,
                tint = resolveColor(style.color) ?: LocalContentColor.current)
        }
    }

    register("textInput") { node, _, formState ->
        val fieldId = node.id ?: ""
        val style = node.style()
        val focusManager = LocalFocusManager.current
        val keyboardType = when (node.props["keyboardType"]?.jsonPrimitive?.contentOrNull) {
            "number" -> KeyboardType.Number
            "email" -> KeyboardType.Email
            "phone" -> KeyboardType.Phone
            else -> KeyboardType.Text
        }
        val hasError = node.errorWhen.isNotEmpty() && !node.errorWhen.all { it.isSatisfied(formState) }
        val errorText = node.props["errorText"]?.jsonPrimitive?.contentOrNull
        OutlinedTextField(
            value = formState[fieldId] ?: "",
            onValueChange = { formState[fieldId] = it },
            label = { Text(node.props["label"]?.jsonPrimitive?.contentOrNull ?: "") },
            modifier = Modifier.applyStyle(style),
            isError = hasError,
            supportingText = if (hasError && errorText != null) { { Text(errorText, color = MaterialTheme.colorScheme.error) } } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )
    }

    register("button") { node, actions, formState ->
        val enabled = node.rules.all { it.isSatisfied(formState) }
        Button(
            onClick = { node.action?.let(actions::handle) },
            enabled = enabled,
            modifier = Modifier.applyStyle(node.style())
        ) {
            Text(node.props["label"]?.jsonPrimitive?.contentOrNull ?: "")
        }
    }

    register("checkbox") { node, _, formState ->
        val fieldId = node.id ?: ""
        val checked = formState[fieldId] == "true"
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.applyStyle(node.style())) {
            Checkbox(checked = checked, onCheckedChange = { formState[fieldId] = it.toString() })
            Text(node.props["label"]?.jsonPrimitive?.contentOrNull ?: "")
        }
    }

    register("switch") { node, _, formState ->
        val fieldId = node.id ?: ""
        val checked = formState[fieldId] == "true"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.applyStyle(node.style()).fillMaxWidth()
        ) {
            Text(node.props["label"]?.jsonPrimitive?.contentOrNull ?: "", modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = { formState[fieldId] = it.toString() })
        }
    }

    register("radioGroup") { node, _, formState ->
        val fieldId = node.id ?: ""
        val selected = formState[fieldId]
        val options = (node.props["options"] as? JsonArray)?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
        Column(modifier = Modifier.applyStyle(node.style())) {
            options.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == option, onClick = { formState[fieldId] = option })
                    Text(option)
                }
            }
        }
    }

    register("dropdown") { node, _, formState ->
        val fieldId = node.id ?: ""
        var expanded by remember { mutableStateOf(false) }
        val options = (node.props["options"] as? JsonArray)?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
        val selected = formState[fieldId] ?: node.props["placeholder"]?.jsonPrimitive?.contentOrNull ?: "Select"
        Box(modifier = Modifier.applyStyle(node.style())) {
            OutlinedButton(onClick = { expanded = true }) { Text(selected) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        formState[fieldId] = option
                        expanded = false
                    })
                }
            }
        }
    }

    register("chip") { node, actions, _ ->
        AssistChip(
            onClick = { node.action?.let(actions::handle) },
            label = { Text(node.props["label"]?.jsonPrimitive?.contentOrNull ?: "") },
            modifier = Modifier.applyStyle(node.style())
        )
    }

    register("badge") { node, _, _ ->
        Badge(modifier = Modifier.applyStyle(node.style())) {
            Text(node.props["count"]?.jsonPrimitive?.contentOrNull ?: "")
        }
    }

    register("progressBar") { node, _, _ ->
        val progress = node.props["progress"]?.jsonPrimitive?.floatOrNull
        val variant = node.props["variant"]?.jsonPrimitive?.contentOrNull
        if (variant == "circular") {
            if (progress != null) CircularProgressIndicator(progress = { progress }) else CircularProgressIndicator()
        } else {
            val modifier = Modifier.applyStyle(node.style()).fillMaxWidth()
            if (progress != null) LinearProgressIndicator(progress = { progress }, modifier = modifier)
            else LinearProgressIndicator(modifier = modifier)
        }
    }

    register("spacer") { node, _, _ ->
        val sizeValue = node.style().size
        Spacer(Modifier.size(if (sizeValue != null) resolveSpacing(sizeValue) else 8.dp))
    }

    register("divider") { _, _, _ -> HorizontalDivider() }

    register("nativeSlot") { node, _, formState ->
        when (node.props["id"]?.jsonPrimitive?.contentOrNull) {
            "balanceToggle" -> BalanceToggle(node)
            else -> {}
        }
    }

    register("dialog") { node, actions, formState ->
        val fieldId = node.id ?: ""
        if (formState[fieldId] == "true") {
            val titleText = node.props["title"]?.jsonPrimitive?.contentOrNull
            AlertDialog(
                onDismissRequest = { formState[fieldId] = "false" },
                title = if (titleText != null) { { Text(titleText) } } else null,
                text = {
                    Column {
                        node.children.forEach { child -> Render(child, actions, formState) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { formState[fieldId] = "false" }) {
                        Text(node.props["confirmLabel"]?.jsonPrimitive?.contentOrNull ?: "OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { formState[fieldId] = "false" }) { Text("Cancel") }
                }
            )
        }
    }

    register("bottomSheet") { node, actions, formState ->
        val fieldId = node.id ?: ""
        val wantVisible = formState[fieldId] == "true"
        val sheetState = rememberModalBottomSheetState()

        LaunchedEffect(wantVisible) {
            if (wantVisible) sheetState.show() else sheetState.hide()
        }

        if (wantVisible || sheetState.isVisible) {
            ModalBottomSheet(onDismissRequest = { formState[fieldId] = "false" }, sheetState = sheetState) {
                Column(Modifier.padding(16.dp)) {
                    node.children.forEach { child -> Render(child, actions, formState) }
                }
            }
        }
    }

    register("slider") { node, _, formState ->
        val fieldId = node.id ?: ""
        val min = node.props["min"]?.jsonPrimitive?.floatOrNull ?: 0f
        val max = node.props["max"]?.jsonPrimitive?.floatOrNull ?: 1f
        val default = node.props["default"]?.jsonPrimitive?.floatOrNull ?: min
        val current = formState[fieldId]?.toFloatOrNull() ?: default
        Slider(
            value = current,
            onValueChange = { formState[fieldId] = it.toString() },
            valueRange = min..max,
            modifier = Modifier.applyStyle(node.style()).fillMaxWidth()
        )
    }

    register("rating") { node, _, _ ->
        val value = node.props["value"]?.jsonPrimitive?.floatOrNull ?: 0f
        val maxStars = node.props["max"]?.jsonPrimitive?.intOrNull ?: 5
        val star = materialIcon("star")
        Row(modifier = Modifier.applyStyle(node.style())) {
            repeat(maxStars) { index ->
                if (star != null) {
                    Icon(
                        imageVector = star,
                        contentDescription = null,
                        tint = if (index < value.toInt()) Color(0xFFFFC107) else Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }

    register("tabs") { node, actions, formState ->
        val fieldId = node.id ?: ""
        val labels = (node.props["labels"] as? JsonArray)?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
        val selected = formState[fieldId]?.toIntOrNull() ?: 0
        Column(modifier = Modifier.applyStyle(node.style())) {
            TabRow(selectedTabIndex = selected) {
                labels.forEachIndexed { index, label ->
                    Tab(
                        selected = selected == index,
                        onClick = { formState[fieldId] = index.toString() },
                        text = { Text(label) }
                    )
                }
            }
            node.children.getOrNull(selected)?.let { child -> Render(child, actions, formState) }
        }
    }

    register("expandable") { node, actions, formState ->
        val fieldId = node.id ?: ""
        val expanded = formState[fieldId] == "true"
        val chevron = materialIcon(if (expanded) "arrowUp" else "arrowDown")
        Column(modifier = Modifier.applyStyle(node.style()).animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { formState[fieldId] = (!expanded).toString() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(node.props["title"]?.jsonPrimitive?.contentOrNull ?: "")
                if (chevron != null) Icon(imageVector = chevron, contentDescription = null)
            }
            if (expanded) {
                Column { node.children.forEach { child -> Render(child, actions, formState) } }
            }
        }
    }

    register("grid") { node, actions, formState ->
        val columnsCount = node.props["columns"]?.jsonPrimitive?.intOrNull ?: 2
        val heightDp = node.props["height"]?.jsonPrimitive?.intOrNull ?: 300
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            modifier = Modifier.applyStyle(node.style()).height(heightDp.dp)
        ) {
            items(node.children) { child -> Render(child, actions, formState) }
        }
    }

    register("list") { node, actions, formState ->
        val heightDp = node.props["height"]?.jsonPrimitive?.intOrNull ?: 300
        LazyColumn(modifier = Modifier.applyStyle(node.style()).height(heightDp.dp)) {
            items(node.children) { child -> Render(child, actions, formState) }
        }
    }


    register("flowRow") { node, actions, formState ->
        val style = node.style()
        FlowRow(modifier = Modifier.applyStyle(style), horizontalArrangement = parseArrangement(style.arrangement)) {
            node.children.forEach { child -> Render(child, actions, formState) }
        }
    }

    register("pager") { node, actions, formState ->
        val heightDp = node.props["height"]?.jsonPrimitive?.intOrNull ?: 200
        val pagerState = rememberPagerState(pageCount = { node.children.size })
        HorizontalPager(state = pagerState, modifier = Modifier.applyStyle(node.style()).height(heightDp.dp)) { page ->
            Render(node.children[page], actions, formState)
        }
    }

    register("otpInput") { node, _, formState ->
        val fieldId = node.id ?: ""
        val length = node.props["length"]?.jsonPrimitive?.intOrNull ?: 6
        val code = formState[fieldId] ?: ""
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.applyStyle(node.style())) {
            repeat(length) { index ->
                OutlinedTextField(
                    value = code.getOrNull(index)?.toString() ?: "",
                    onValueChange = { input ->
                        val digit = input.lastOrNull()
                        val chars = code.padEnd(length, ' ').toMutableList()
                        if (index < chars.size) chars[index] = digit ?: ' '
                        formState[fieldId] = chars.joinToString("").trimEnd()
                    },
                    modifier = Modifier.width(48.dp),
                    singleLine = true
                )
            }
        }
    }

    register("datePicker") { node, _, formState ->
        val fieldId = node.id ?: ""
        var showDialog by remember { mutableStateOf(false) }
        val selectedMillis = formState[fieldId]?.toLongOrNull()
        val label = node.props["label"]?.jsonPrimitive?.contentOrNull ?: "Select date"
        OutlinedButton(onClick = { showDialog = true }, modifier = Modifier.applyStyle(node.style())) {
            Text(if (selectedMillis != null) "Date selected" else label)
        }
        if (showDialog) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedMillis)
            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { formState[fieldId] = it.toString() }
                        showDialog = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }

    register("searchBar") { node, _, formState ->
        val fieldId = node.id ?: ""
        val query = formState[fieldId] ?: ""
        var expanded by remember { mutableStateOf(false) }
        val placeholder = node.props["placeholder"]?.jsonPrimitive?.contentOrNull ?: "Search"
        SearchBar(
            query = query,
            onQueryChange = { formState[fieldId] = it },
            onSearch = { expanded = false },
            active = expanded,
            onActiveChange = { expanded = it },
            placeholder = { Text(placeholder) },
            modifier = Modifier.applyStyle(node.style())
        ) {}
    }

    register("skeleton") { node, _, _ ->
        val style = node.style()
        ShimmerBox(
            modifier = Modifier.applyStyle(style).height((node.props["height"]?.jsonPrimitive?.intOrNull ?: 16).dp),
            cornerRadius = style.cornerRadius ?: 4
        )
    }




    register("snackbar") { node, _, formState ->
        val fieldId = node.id ?: ""
        val visible = formState[fieldId] == "true"
        val message = node.props["message"]?.jsonPrimitive?.contentOrNull ?: ""
        val hostState = LocalSnackBarHostState.current
        LaunchedEffect(visible) {
            if (visible) {
                hostState?.showSnackbar(message)
                formState[fieldId] = "false"
            }
        }
    }
}

@Composable
private fun BalanceToggle(node: com.example.sdui.shared.UiNode) {
    var visible by remember { mutableStateOf(true) }
    val amount = node.props["amount"]?.jsonPrimitive?.contentOrNull ?: ""
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedContent(targetState = visible, label = "balanceVisibility") { isVisible ->
            Text(if (isVisible) amount else "\u2022\u2022\u2022\u2022\u2022\u2022", fontSize = 32.sp)
        }
        Text(
            if (visible) " \uD83D\uDC41" else " \uD83D\uDE48",
            modifier = Modifier.clickable { visible = !visible }
        )
    }
}