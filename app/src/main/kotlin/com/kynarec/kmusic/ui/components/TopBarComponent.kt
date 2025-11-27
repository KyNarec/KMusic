package com.kynarec.kmusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kynarec.kmusic.R
import com.kynarec.kmusic.ui.HomeScreen
import com.kynarec.kmusic.ui.SearchScreen
import com.kynarec.kmusic.ui.SettingsScreen
import com.kynarec.kmusic.ui.screens.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarComponent(
    isSearchScreen: Boolean,
    navController: NavHostController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .fillMaxHeight(0.1f)
    ){
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .clickable(true, onClick = {
                    navController.navigate(HomeScreen)
                })
        ){
            Row {
                if (isSearchScreen) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clickable(onClick = { /*TODO*/ })
                        .height(60.dp)
                        .width(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground_scaled),
                        contentDescription = "Logo",
                        // 3. Set the size of the *actual icon* content
                        modifier = Modifier
                            .size(40.dp)
                            .padding(bottom = 6.dp, top = 0.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = stringResource(R.string.app_name),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 35.sp,
                    modifier = Modifier
                        .padding(0.dp, 0.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = { navController.navigate(SearchScreen) },
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }
        IconButton(
            onClick = { navController.navigate(SettingsScreen) },
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}
