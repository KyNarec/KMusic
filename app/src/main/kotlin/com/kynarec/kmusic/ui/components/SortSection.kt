package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kynarec.kmusic.ui.screens.song.SortOption

@ExperimentalMaterial3ExpressiveApi
@Composable
fun SortSection(
    sortOptions: List<SortOption>,
    selectedSortOption: SortOption,
    onOptionSelected: (SortOption) -> Unit
) {
    // SingleChoiceSegmentedButtonRow handles the "connected" border look
    LazyRow (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(sortOptions) { index, sortOption ->
            ToggleButton(
                modifier = Modifier.padding(horizontal = 4.dp),
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