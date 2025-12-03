package com.kynarec.kmusic.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.ui.viewModels.MusicViewModel

@Composable
fun AlbumsScreen(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel,
    database: KmusicDatabase,
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

}