package com.kynarec.kmusic.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedColumn(
    modifier: Modifier = Modifier,
    items: List<@Composable () -> Unit>,
    spacerBackground: Color = MaterialTheme.colorScheme.surfaceContainer,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Column(
        modifier = modifier,
    ) {
        items.forEachIndexed { index, item ->
            Card(
                shape = RoundedCornerShape(
                    topStart = if (index == 0) 16.dp else 6.dp,
                    topEnd = if (index == 0) 16.dp else 6.dp,
                    bottomEnd = if (index == items.lastIndex) 16.dp else 6.dp,
                    bottomStart = if (index == items.lastIndex) 16.dp else 6.dp
                ),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = containerColor
                )
            ) {
                item()
            }
            if (index != items.lastIndex) {
                Spacer(Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(spacerBackground)
                )
            }
        }
    }
}