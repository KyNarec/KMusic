package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kynarec.kmusic.ui.screens.song.FilterOption
import com.kynarec.kmusic.ui.theme.KMusicTheme

@ExperimentalMaterial3ExpressiveApi
@Composable
fun SortSection(
    filterOptions: List<FilterOption>,
    selectedFilterOption: FilterOption,
    onOptionSelected: (FilterOption) -> Unit
) {
    LazyRow (
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        itemsIndexed(filterOptions, key = { _, sortOption -> sortOption.text }) { index, sortOption ->
            ToggleButton(
                shapes =  when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    filterOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                onCheckedChange = { onOptionSelected(sortOption) },
                checked = selectedFilterOption == sortOption,
                modifier = Modifier.semantics { role = Role.RadioButton },
                ) {
                Text(
                    text = sortOption.text,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = false)
@Composable
fun PreviewSortSelection() {
    KMusicTheme(
        darkTheme = true,
        dynamicColor = false
    ) {
        Scaffold { paddingValues ->
            Box(Modifier.padding(paddingValues)) {

                val filterOptions = listOf(
                    FilterOption("Song"),
                    FilterOption("Album"),
                    FilterOption("Artist"),
                    FilterOption("Playlist"),
                    FilterOption("Videos"),
                    FilterOption("Podcasts"),
                )
                var selectedSortOption by remember { mutableStateOf(filterOptions[2])}
                SortSection(
                    filterOptions = filterOptions,
                    selectedFilterOption = selectedSortOption,
                    onOptionSelected = { selectedSortOption = it }
                )
            }
        }
    }
}