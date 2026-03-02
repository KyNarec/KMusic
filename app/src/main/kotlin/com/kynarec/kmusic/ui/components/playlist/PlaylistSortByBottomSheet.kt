package com.kynarec.kmusic.ui.components.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kynarec.kmusic.enums.SortBy
import com.kynarec.kmusic.ui.viewModels.PlaylistOfflineDetailActions

@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistSortByBottomSheet(
    onClick: (SortBy) -> Unit = {},
    onDismiss: (PlaylistOfflineDetailActions) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismiss(PlaylistOfflineDetailActions.TogglePlaylistSortByBottomSheet) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            item {
                Text("Sort by",
                    style = MaterialTheme.typography.headlineLargeEmphasized,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            items(SortBy.entries) { sortBy ->
                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable(onClick = {
                        onClick(sortBy)
                        onDismiss(PlaylistOfflineDetailActions.TogglePlaylistSortByBottomSheet)
                    }),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sortBy.getIcon()
                    Spacer(Modifier.fillMaxWidth(0.03f))
                    Text(sortBy.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}