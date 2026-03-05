package com.kynarec.kmusic.ui.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kynarec.kmusic.ui.screens.song.SortOption
import com.kynarec.kmusic.ui.theme.KMusicTheme

@ExperimentalMaterial3ExpressiveApi
@Composable
fun SortSection(
    sortOptions: List<SortOption>,
    selectedSortOption: SortOption,
    onOptionSelected: (SortOption) -> Unit
) {
    LazyRow (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(sortOptions, key = { _, sortOption -> sortOption.text }) { index, sortOption ->

            ToggleButton(
                modifier = Modifier.padding(horizontal = 2.dp),
                shapes =  when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    sortOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                onCheckedChange = {onOptionSelected(sortOption) },
                checked = selectedSortOption == sortOption,
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

                val sortOptions = listOf(
                    SortOption("All"),
                    SortOption("Favorites"),
                    SortOption("Listened"),
                    SortOption("Downloads"),
                )
                var selectedSortOption by remember { mutableStateOf(sortOptions[2])}
                SortSection(
                    sortOptions = sortOptions,
                    selectedSortOption = selectedSortOption,
                    onOptionSelected = { selectedSortOption = it }
                )
            }
        }
    }
}