package com.kynarec.kmusic.ui.components.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kynarec.kmusic.ui.theme.KMusicTheme


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun ButtonTest() {
    KMusicTheme() {
        Scaffold() { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                val numButtons = 10
                ButtonGroup(
                    overflowIndicator = { menuState ->
                        ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                    },
                    verticalAlignment = Alignment.Top,
                ) {
                    for (i in 0 until numButtons) {
                        clickableItem(onClick = {}, label = "$i")

                    }
                }

                val options = listOf("Work", "Restaurant", "Coffee")
                val unCheckedIcons =
                    listOf(Icons.Outlined.Work, Icons.Outlined.Restaurant, Icons.Outlined.Coffee)
                val checkedIcons =
                    listOf(Icons.Filled.Work, Icons.Filled.Restaurant, Icons.Filled.Coffee)
                var selectedIndex by remember { mutableIntStateOf(0) }

                Row(
                    Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    val modifiers =
                        listOf(Modifier.weight(1f), Modifier.weight(1.5f), Modifier.weight(1f))

                    options.forEachIndexed { index, label ->
                        ToggleButton(
                            checked = selectedIndex == index,
                            onCheckedChange = { selectedIndex = index },
                            modifier = modifiers[index].semantics { role = Role.RadioButton },
                            shapes =
                                when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                        ) {
                            Icon(
                                if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index],
                                contentDescription = "Localized description",
                            )
                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                            Text(label)
                        }
                    }
                }

                val options1 = listOf("Work", "Restaurant", "Coffee", "Search", "Home")
                val unCheckedIcons1 =
                    listOf(
                        Icons.Outlined.Work,
                        Icons.Outlined.Restaurant,
                        Icons.Outlined.Coffee,
                        Icons.Outlined.Search,
                        Icons.Outlined.Home,
                    )
                val checkedIcons1 =
                    listOf(
                        Icons.Filled.Work,
                        Icons.Filled.Restaurant,
                        Icons.Filled.Coffee,
                        Icons.Filled.Search,
                        Icons.Filled.Home,
                    )
                var selectedIndex1 by remember { mutableIntStateOf(0) }

                FlowRow(
                    Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    options1.forEachIndexed { index, label ->
                        ToggleButton(
                            checked = selectedIndex1 == index,
                            onCheckedChange = { selectedIndex1 = index },
                            shapes =
                                when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    options1.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                            modifier = Modifier.semantics { role = Role.RadioButton },
                        ) {
                            Icon(
                                if (selectedIndex1 == index) checkedIcons1[index] else unCheckedIcons1[index],
                                contentDescription = "Localized description",
                            )
                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                            Text(label)
                        }
                    }
                }
                FourthExample()
                FifthExample()
                SixthExample()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FourthExample(modifier: Modifier = Modifier) {
    val options = listOf("Work", "Restaurant", "Coffee")
    val unCheckedIcons =
        listOf(Icons.Outlined.Work, Icons.Outlined.Restaurant, Icons.Outlined.Coffee)
    val checkedIcons = listOf(Icons.Filled.Work, Icons.Filled.Restaurant, Icons.Filled.Coffee)
    val checked = remember { mutableStateListOf(false, false, false) }

    Row(
        Modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        val modifiers = listOf(Modifier.weight(1f), Modifier.weight(1.5f), Modifier.weight(1f))
        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = checked[index],
                onCheckedChange = { checked[index] = it },
                modifier = modifiers[index],
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
            ) {
                Icon(
                    if (checked[index]) checkedIcons[index] else unCheckedIcons[index],
                    contentDescription = "Localized description",
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(label)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FifthExample(modifier: Modifier = Modifier) {
    val options = listOf("Work", "Restaurant", "Coffee", "Search", "Home")
    val unCheckedIcons =
        listOf(
            Icons.Outlined.Work,
            Icons.Outlined.Restaurant,
            Icons.Outlined.Coffee,
            Icons.Outlined.Search,
            Icons.Outlined.Home,
        )
    val checkedIcons =
        listOf(
            Icons.Filled.Work,
            Icons.Filled.Restaurant,
            Icons.Filled.Coffee,
            Icons.Filled.Search,
            Icons.Filled.Home,
        )
    val checked = remember { mutableStateListOf(false, false, false, false, false) }

    FlowRow(
        Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = checked[index],
                onCheckedChange = { checked[index] = it },
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
            ) {
                Icon(
                    if (checked[index]) checkedIcons[index] else unCheckedIcons[index],
                    contentDescription = "Localized description",
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(label)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SixthExample(modifier: Modifier = Modifier) {
    val options = listOf("Home", "Songs", "Artists", "Albums", "Playlists")
    var selectedIndex by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy((-6).dp)) {
        options.forEachIndexed { index, label ->
            val shape =
                when (index) {
                    0 ->
                        (ButtonGroupDefaults.connectedMiddleButtonShapes().shape
                                as RoundedCornerShape)
                            .copy(topStart = CornerSize(100), topEnd = CornerSize(100))

                    options.lastIndex ->
                        (ButtonGroupDefaults.connectedMiddleButtonShapes().shape
                                as RoundedCornerShape)
                            .copy(bottomStart = CornerSize(100), bottomEnd = CornerSize(100))

                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes().shape
                }
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { selectedIndex = index },
                shapes =
                    ToggleButtonDefaults.shapes(
                        shape = shape,
                        checkedShape = ButtonGroupDefaults.connectedButtonCheckedShape,
                    ),
                modifier = Modifier.semantics { role = Role.RadioButton },
            ) {
                Text(label)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ButtonGroupScope.Idk() {
    this
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun MultiSelectionSegmentedListItemSample() {
    val count = 4
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        repeat(count) { idx ->
            var checked by rememberSaveable { mutableStateOf(false) }
            SegmentedListItem(
                checked = checked,
                onCheckedChange = { checked = it },
                colors = colors,
                shapes = ListItemDefaults.segmentedShapes(index = idx, count = count),
                leadingContent = { Checkbox(checked = checked, onCheckedChange = null) },
                trailingContent = { Icon(Icons.Default.Favorite, contentDescription = null) },
                supportingContent = { Text("Additional info") },
                content = { Text("Item ${idx + 1}") },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun SegmentedListItemWithExpansionSample() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val numChildren = 3
    val itemCount = 1 + if (expanded) numChildren else 0
    val childrenChecked = rememberSaveable { mutableStateListOf(*Array(numChildren) { false }) }
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        Spacer(Modifier.height(100.dp))
        SegmentedListItem(
            onClick = { expanded = !expanded },
            modifier =
                Modifier.semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" },
            colors = colors,
            shapes = ListItemDefaults.segmentedShapes(index = 0, count = itemCount),
            leadingContent = { Icon(Icons.Default.Favorite, contentDescription = null) },
            trailingContent = {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                )
            },
            content = { Text("Click to expand/collapse") },
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
            exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                repeat(numChildren) { idx ->
                    SegmentedListItem(
                        checked = childrenChecked[idx],
                        onCheckedChange = { childrenChecked[idx] = it },
                        colors = colors,
                        shapes =
                            ListItemDefaults.segmentedShapes(index = idx + 1, count = itemCount),
                        leadingContent = {
                            Icon(Icons.Default.Favorite, contentDescription = null)
                        },
                        trailingContent = {
                            Checkbox(checked = childrenChecked[idx], onCheckedChange = null)
                        },
                        content = { Text("Child ${idx + 1}") },
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun SegmentedListItems() {
    KMusicTheme(dynamicColor = false) {
        val count = 4
        val colors =
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            SegmentedListItem(
                onClick = {},
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = count),
                colors = colors,
                leadingContent = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                content = { Text("One line list item") },
            )
            SegmentedListItem(
                onClick = {},
                shapes = ListItemDefaults.segmentedShapes(index = 1, count = count),
                colors = colors,
                leadingContent = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                supportingContent = { Text("Supporting text") },
                content = { Text("Two line list item") },
            )
            SegmentedListItem(
                onClick = {},
                shapes = ListItemDefaults.segmentedShapes(index = 2, count = count),
                colors = colors,
                leadingContent = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                overlineContent = { Text("Overline text") },
                supportingContent = { Text("Supporting text") },
                content = { Text("Three line list item") },
            )
            SegmentedListItem(
                onClick = {},
                shapes = ListItemDefaults.segmentedShapes(index = 3, count = count),
                colors = colors,
                leadingContent = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                supportingContent = { Text("Supporting text\nthat is multiple lines") },
                content = { Text("Another three line list item") },
            )
        }
    }
}

@Composable
@Preview
fun TestingItems() {
    KMusicTheme(dynamicColor = false) {
        Scaffold() { contentPadding ->
            Box(
                Modifier
                    .padding(contentPadding)
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        BorderStroke(2.dp, MaterialTheme.colorScheme.tertiaryContainer),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(start = 2.dp, top = 4.dp, bottom = 4.dp, end = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Release Notes",
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}