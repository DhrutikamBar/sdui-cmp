package com.dhruti.sdui.sdk

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.alexzhirkevich.compottie.*
import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiNode

val LocalSnackBarHostState = compositionLocalOf<SnackbarHostState?> { null }
val LocalResourceResolver = compositionLocalOf<ResourceResolver?> { null }

fun String.resolve(): String {
    if (!isResource()) return this
    return this 
}

fun Modifier.applySemantics(node: UiNode): Modifier {
    val s = node.semantics ?: return this
    return this.semantics {
        s.contentDescription?.let { contentDescription = it }
        s.role?.let {
            when (it) {
                "button" -> role = Role.Button
                "image" -> role = Role.Image
                "checkbox" -> role = Role.Checkbox
                "switch" -> role = Role.Switch
                "radioButton" -> role = Role.RadioButton
                "tab" -> role = Role.Tab
                "header" -> heading()
            }
        }
    }
}

private fun SduiValue?.asString() = (this as? SduiValue.StringValue)?.value ?: ""
private fun SduiValue?.asFloat() = (this as? SduiValue.NumberValue)?.value?.toFloat()
private fun SduiValue?.asInt() = (this as? SduiValue.NumberValue)?.value?.toInt()
private fun SduiValue?.asBoolean() = (this as? SduiValue.BooleanValue)?.value ?: false
private fun SduiValue?.asList() = (this as? SduiValue.ListValue)?.value ?: emptyList()

private fun UiNode.getContentDescription(): String? {
    return semantics?.contentDescription ?: props["contentDescription"].asString().takeIf { it.isNotEmpty() }
}

@OptIn(ExperimentalMaterial3Api::class)
fun ComponentRegistry.registerCoreWidgets() {

    register("column") { node, actions, formState ->
        val style = node.style()
        val isInsideScrollable = LocalIsInsideScrollable.current
        var modifier = Modifier.applyStyle(style).applySemantics(node)
        if (style.animateSize == true) modifier = modifier.animateContentSize()
        if (style.scrollable == true && !isInsideScrollable) modifier = modifier.verticalScroll(rememberScrollState())
        if (node.action != null) modifier = modifier.clickable { node.action?.let(actions::handle) }
        Column(modifier = modifier, horizontalAlignment = parseColumnAlignment(style.alignment)) {
            node.children.forEach { child -> Render(child, actions, formState) }
        }
    }

    register("row") { node, actions, formState ->
        val style = node.style()
        val isInsideScrollable = LocalIsInsideScrollable.current
        var modifier = Modifier.applyStyle(style).applySemantics(node)
        if (style.animateSize == true) modifier = modifier.animateContentSize()
        if (style.scrollable == true && !isInsideScrollable) modifier = modifier.horizontalScroll(rememberScrollState())
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
        var base = Modifier.applyStyle(style).applySemantics(node)
        if (style.animateSize == true) base = base.animateContentSize()
        val clickableModifier = if (node.action != null) {
            base.clickable { node.action?.let(actions::handle) }
        } else base
        Box(modifier = clickableModifier, contentAlignment = parseBoxAlignment(style.alignment)) {
            node.children.forEach { child -> Render(child, actions, formState) }
        }
    }

    register("text") { node, actions, _ ->
        val style = node.style()
        var modifier = Modifier.applyStyle(style).applySemantics(node)
        if (node.action != null) modifier = modifier.clickable { node.action?.let(actions::handle) }
        StyledText(
            value = node.props["value"].asString(),
            style = style,
            modifier = modifier
        )
    }

    register("image") { node, actions, _ ->
        val style = node.style()
        val resolver = LocalResourceResolver.current
        val url = node.props["url"].asString().takeIf { it.isNotEmpty() }
        val emoji = node.props["icon"].asString().takeIf { it.isNotEmpty() }
        val base = Modifier.applyStyle(style).applySemantics(node)
        val clickableModifier = if (node.action != null) {
            base.clickable { node.action?.let(actions::handle) }
        } else base
        when {
            url != null -> {
                val finalModel = if (url.isResource()) resolver?.resolveImage(url) ?: url else url
                AsyncImage(
                    model = finalModel,
                    contentDescription = node.getContentDescription(),
                    modifier = clickableModifier,
                    contentScale = ContentScale.Crop
                )
            }
            emoji != null -> Box(modifier = clickableModifier, contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = (style.fontSize ?: 20).sp, textAlign = TextAlign.Center)
            }
            else -> {}
        }
    }

    register("icon") { node, actions, _ ->
        val style = node.style()
        val vector = materialIcon(node.props["name"].asString())
        val description = node.getContentDescription()
        var base = Modifier.applyStyle(style).applySemantics(node)
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
        val keyboardType = when (node.props["keyboardType"].asString()) {
            "number" -> KeyboardType.Number
            "email" -> KeyboardType.Email
            "phone" -> KeyboardType.Phone
            else -> KeyboardType.Text
        }
        val hasError = node.errorWhen.any { it.evaluate(formState) }
        val errorText = node.props["errorText"].asString()
        OutlinedTextField(
            value = formState.getString(fieldId),
            onValueChange = { formState.setString(fieldId, it) },
            label = { Text(node.props["label"].asString()) },
            modifier = Modifier.applyStyle(style).applySemantics(node).fillMaxWidth(),
            isError = hasError,
            supportingText = if (hasError && errorText.isNotEmpty()) { { Text(errorText, color = MaterialTheme.colorScheme.error) } } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
        )
    }

    register("button") { node, actions, formState ->
        val enabled = node.rules.all { it.evaluate(formState) }
        Button(
            onClick = { node.action?.let(actions::handle) },
            enabled = enabled,
            modifier = Modifier.applyStyle(node.style()).applySemantics(node)
        ) {
            Text(node.props["label"].asString())
        }
    }

    register("checkbox") { node, _, formState ->
        val fieldId = node.id ?: ""
        val checked = formState[fieldId] is SduiValue.BooleanValue && (formState[fieldId] as SduiValue.BooleanValue).value
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.applyStyle(node.style()).applySemantics(node)) {
            Checkbox(checked = checked, onCheckedChange = { formState[fieldId] = SduiValue.BooleanValue(it) })
            Text(node.props["label"].asString())
        }
    }

    register("switch") { node, _, formState ->
        val fieldId = node.id ?: ""
        val checked = formState[fieldId] is SduiValue.BooleanValue && (formState[fieldId] as SduiValue.BooleanValue).value
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.applyStyle(node.style()).fillMaxWidth().applySemantics(node)
        ) {
            Text(node.props["label"].asString(), modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = { formState[fieldId] = SduiValue.BooleanValue(it) })
        }
    }

    register("radioGroup") { node, _, formState ->
        val fieldId = node.id ?: ""
        val selected = formState.getString(fieldId)
        val options = node.props["options"].asList().map { it.asString() }
        Column(modifier = Modifier.applyStyle(node.style()).applySemantics(node)) {
            options.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == option, onClick = { formState.setString(fieldId, option) })
                    Text(option)
                }
            }
        }
    }

    register("dropdown") { node, _, formState ->
        val fieldId = node.id ?: ""
        var expanded by remember { mutableStateOf(false) }
        val options = node.props["options"].asList().map { it.asString() }
        val selected = formState.getString(fieldId).takeIf { it.isNotEmpty() } ?: node.props["placeholder"].asString().takeIf { it.isNotEmpty() } ?: "Select"
        Box(modifier = Modifier.applyStyle(node.style()).applySemantics(node)) {
            OutlinedButton(onClick = { expanded = true }) { Text(selected) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        formState.setString(fieldId, option)
                        expanded = false
                    })
                }
            }
        }
    }

    register("chip") { node, actions, _ ->
        AssistChip(
            onClick = { node.action?.let(actions::handle) },
            label = { Text(node.props["label"].asString()) },
            modifier = Modifier.applyStyle(node.style()).applySemantics(node)
        )
    }

    register("badge") { node, _, _ ->
        Badge(modifier = Modifier.applyStyle(node.style()).applySemantics(node)) {
            Text(node.props["count"].asString())
        }
    }

    register("progressBar") { node, _, _ ->
        val progress = node.props["progress"].asFloat()
        val variant = node.props["variant"].asString()
        if (variant == "circular") {
            if (progress != null) CircularProgressIndicator(progress = { progress }, modifier = Modifier.applySemantics(node)) else CircularProgressIndicator(modifier = Modifier.applySemantics(node))
        } else {
            val modifier = Modifier.applyStyle(node.style()).fillMaxWidth().applySemantics(node)
            if (progress != null) LinearProgressIndicator(progress = { progress }, modifier = modifier)
            else LinearProgressIndicator(modifier = modifier)
        }
    }

    register("spacer") { node, _, _ ->
        val sizeValue = node.style().size
        Spacer(Modifier.size(if (sizeValue != null) resolveSpacing(sizeValue) else 8.dp).applySemantics(node))
    }

    register("divider") { node, _, _ -> HorizontalDivider(Modifier.applySemantics(node)) }

    register("nativeSlot") { node, _, _ ->
        // Host app can override this or we can provide a generic mechanism
    }

    register("dialog") { node, actions, formState ->
        val fieldId = node.id ?: ""
        if (formState[fieldId] is SduiValue.BooleanValue && (formState[fieldId] as SduiValue.BooleanValue).value) {
            val titleText = node.props["title"].asString()
            AlertDialog(
                onDismissRequest = { formState[fieldId] = SduiValue.BooleanValue(false) },
                title = if (titleText.isNotEmpty()) { { Text(titleText) } } else null,
                text = {
                    Column(Modifier.applySemantics(node)) {
                        node.children.forEach { child -> Render(child, actions, formState) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { formState[fieldId] = SduiValue.BooleanValue(false) }) {
                        Text(node.props["confirmLabel"].asString().takeIf { it.isNotEmpty() } ?: "OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { formState[fieldId] = SduiValue.BooleanValue(false) }) { Text("Cancel") }
                }
            )
        }
    }

    register("bottomSheet") { node, actions, formState ->
        val fieldId = node.id ?: ""
        val wantVisible = formState[fieldId] is SduiValue.BooleanValue && (formState[fieldId] as SduiValue.BooleanValue).value
        val sheetState = rememberModalBottomSheetState()

        LaunchedEffect(wantVisible) {
            if (wantVisible) sheetState.show() else sheetState.hide()
        }

        if (wantVisible || sheetState.isVisible) {
            ModalBottomSheet(onDismissRequest = { formState[fieldId] = SduiValue.BooleanValue(false) }, sheetState = sheetState) {
                Column(Modifier.padding(16.dp).applySemantics(node)) {
                    node.children.forEach { child -> Render(child, actions, formState) }
                }
            }
        }
    }

    register("slider") { node, _, formState ->
        val fieldId = node.id ?: ""
        val min = node.props["min"].asFloat() ?: 0f
        val max = node.props["max"].asFloat() ?: 1f
        val default = node.props["default"].asFloat() ?: min
        val current = (formState[fieldId] as? SduiValue.NumberValue)?.value?.toFloat() ?: default
        Slider(
            value = current,
            onValueChange = { formState[fieldId] = SduiValue.NumberValue(it.toDouble()) },
            valueRange = min..max,
            modifier = Modifier.applyStyle(node.style()).fillMaxWidth().applySemantics(node)
        )
    }

    register("rating") { node, _, _ ->
        val value = node.props["value"].asFloat() ?: 0f
        val maxStars = node.props["max"].asInt() ?: 5
        val star = materialIcon("star")
        Row(modifier = Modifier.applyStyle(node.style()).applySemantics(node)) {
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
        val labels = node.props["labels"].asList().map { it.asString() }
        val selected = (formState[fieldId] as? SduiValue.NumberValue)?.value?.toInt() ?: 0
        Column(modifier = Modifier.applyStyle(node.style()).applySemantics(node)) {
            TabRow(selectedTabIndex = selected) {
                labels.forEachIndexed { index, label ->
                    Tab(
                        selected = selected == index,
                        onClick = { formState[fieldId] = SduiValue.NumberValue(index.toDouble()) },
                        text = { Text(label) }
                    )
                }
            }
            node.children.getOrNull(selected)?.let { child -> Render(child, actions, formState) }
        }
    }

    register("expandable") { node, actions, formState ->
        val fieldId = node.id ?: ""
        val expanded = formState[fieldId] is SduiValue.BooleanValue && (formState[fieldId] as SduiValue.BooleanValue).value
        val chevron = materialIcon(if (expanded) "arrowUp" else "arrowDown")
        Column(modifier = Modifier.applyStyle(node.style()).animateContentSize().applySemantics(node)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { formState[fieldId] = SduiValue.BooleanValue(!expanded) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(node.props["title"].asString())
                if (chevron != null) Icon(imageVector = chevron, contentDescription = null)
            }
            if (expanded) {
                Column { node.children.forEach { child -> Render(child, actions, formState) } }
            }
        }
    }

    register("grid") { node, actions, formState ->
        val columnsCount = node.props["columns"].asInt() ?: 2
        val heightDp = node.props["height"].asInt() ?: 300
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsCount),
            modifier = Modifier.applyStyle(node.style()).height(heightDp.dp).applySemantics(node)
        ) {
            items(node.children) { child -> Render(child, actions, formState) }
        }
    }

    register("list") { node, actions, formState ->
        val heightDp = node.props["height"].asInt() ?: 300
        LazyColumn(modifier = Modifier.applyStyle(node.style()).height(heightDp.dp).applySemantics(node)) {
            items(node.children) { child -> Render(child, actions, formState) }
        }
    }


    register("flowRow") { node, actions, formState ->
        val style = node.style()
        FlowRow(modifier = Modifier.applyStyle(style).applySemantics(node), horizontalArrangement = parseArrangement(style.arrangement)) {
            node.children.forEach { child -> Render(child, actions, formState) }
        }
    }

    register("pager") { node, actions, formState ->
        val heightDp = node.props["height"].asInt() ?: 200
        val pagerState = rememberPagerState(pageCount = { node.children.size })
        HorizontalPager(state = pagerState, modifier = Modifier.applyStyle(node.style()).height(heightDp.dp).applySemantics(node)) { page ->
            Render(node.children[page], actions, formState)
        }
    }

    register("otpInput") { node, _, formState ->
        val fieldId = node.id ?: ""
        val length = node.props["length"].asInt() ?: 6
        val code = formState.getString(fieldId)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.applyStyle(node.style()).applySemantics(node)) {
            repeat(length) { index ->
                OutlinedTextField(
                    value = code.getOrNull(index)?.toString() ?: "",
                    onValueChange = { input ->
                        val digit = input.lastOrNull()
                        val chars = code.padEnd(length, ' ').toMutableList()
                        if (index < chars.size) chars[index] = digit ?: ' '
                        formState.setString(fieldId, chars.joinToString("").trimEnd())
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
        val selectedMillis = (formState[fieldId] as? SduiValue.NumberValue)?.value?.toLong()
        val label = node.props["label"].asString().takeIf { it.isNotEmpty() } ?: "Select date"
        OutlinedButton(onClick = { showDialog = true }, modifier = Modifier.applyStyle(node.style()).applySemantics(node)) {
            Text(if (selectedMillis != null) "Date selected" else label)
        }
        if (showDialog) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedMillis)
            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { formState[fieldId] = SduiValue.NumberValue(it.toDouble()) }
                        showDialog = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
            ) { DatePicker(state = datePickerState) }
        }
    }

    register("searchBar") { node, _, formState ->
        val fieldId = node.id ?: ""
        val query = formState.getString(fieldId)
        var expanded by remember { mutableStateOf(false) }
        val placeholder = node.props["placeholder"].asString().takeIf { it.isNotEmpty() } ?: "Search"
        SearchBar(
            query = query,
            onQueryChange = { formState.setString(fieldId, it) },
            onSearch = { expanded = false },
            active = expanded,
            onActiveChange = { expanded = it },
            placeholder = { Text(placeholder) },
            modifier = Modifier.applyStyle(node.style()).applySemantics(node)
        ) {}
    }

    register("skeleton") { node, _, _ ->
        val style = node.style()
        ShimmerBox(
            modifier = Modifier.applyStyle(style).height((node.props["height"].asInt() ?: 16).dp).applySemantics(node),
            cornerRadius = style.cornerRadius ?: 4
        )
    }

    register("lottieAnimation") { node, _, _ ->
        val url = node.props["url"].asString()
        val loop = node.props["loop"].asBoolean()
        
        val result = rememberLottieComposition(spec = LottieCompositionSpec.Url(url))
        
        when {
            result.isLoading -> {
                ShimmerBox(Modifier.applyStyle(node.style()))
            }
            result.isSuccess -> {
                val composition = result.value
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = if (loop) Compottie.IterateForever else 1
                )
                Image(
                    painter = rememberLottiePainter(
                        composition = composition,
                        progress = { progress }
                    ),
                    contentDescription = node.getContentDescription(),
                    modifier = Modifier.applyStyle(node.style())
                )
            }
            result.isFailure -> {
                // Graceful failure: show nothing
                Box(Modifier.applyStyle(node.style()))
            }
        }
    }

    register("snackbar") { node, _, formState ->
        val fieldId = node.id ?: ""
        val visible = formState[fieldId] is SduiValue.BooleanValue && (formState[fieldId] as SduiValue.BooleanValue).value
        val message = node.props["message"].asString()
        val hostState = LocalSnackBarHostState.current
        LaunchedEffect(visible) {
            if (visible) {
                hostState?.showSnackbar(message)
                formState[fieldId] = SduiValue.BooleanValue(false)
            }
        }
    }
}
