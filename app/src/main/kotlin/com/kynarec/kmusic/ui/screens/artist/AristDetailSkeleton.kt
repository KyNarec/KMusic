package com.kynarec.kmusic.ui.screens.artist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kynarec.kmusic.ui.components.MarqueeBox
import com.kynarec.kmusic.ui.components.album.AlbumComponentSkeleton
import com.kynarec.kmusic.ui.components.song.SongComponentSkeleton
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.utils.shimmerEffect
import org.koin.compose.viewmodel.koinActivityViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistDetailSkeleton(
    viewModel: MusicViewModel = koinActivityViewModel()
) {
    val showControlBar = viewModel.uiState.collectAsStateWithLifecycle().value.showControlBar

    LazyColumn {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEffect()
            )
        }
        item {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "",
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
            }
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarqueeBox(
                    boxModifier = Modifier
                        .weight(1f),
                    text = "Top Songs",
                    style = MaterialTheme.typography.titleLargeEmphasized.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                )

                IconButton(
                    onClick = {
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle"
                    )
                }

                IconButton(
                    onClick = {
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options"
                    )
                }
                IconButton(
                    onClick = {
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Show all Songs"
                    )
                }
            }
        }
        items(5) {
            SongComponentSkeleton()
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarqueeBox(
                    boxModifier = Modifier
                        .weight(1f),
                    text = "Albums",
                    style = MaterialTheme.typography.titleLargeEmphasized.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                )
                IconButton(
                    onClick = {
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Show all Songs"
                    )
                }
            }
        }

        item {
            LazyRow {
                items(8) {
                    AlbumComponentSkeleton()
                }
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarqueeBox(
                    boxModifier = Modifier
                        .weight(1f),
                    text = "Singles & EPs",
                    style = MaterialTheme.typography.titleLargeEmphasized.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                )
                IconButton(
                    onClick = {
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Show all Songs"
                    )
                }
            }
        }

        item {
            LazyRow {
                items(8) {
                    AlbumComponentSkeleton()
                }
            }
        }

        if (showControlBar)
            item {
                Spacer(Modifier.height(70.dp))
            }

    }
}