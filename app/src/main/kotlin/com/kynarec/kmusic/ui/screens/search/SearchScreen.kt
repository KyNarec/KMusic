package com.kynarec.kmusic.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.kynarec.kmusic.R
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.SearchQuery
import com.kynarec.kmusic.service.innertube.searchSuggestions
import com.kynarec.kmusic.ui.SearchResultScreen
import com.kynarec.kmusic.ui.components.MarqueeBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    navController: NavHostController,
    query: String? = null
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var searchQueries by remember { mutableStateOf(emptyList<SearchQuery>()) }

    val searchQueryDao = KmusicDatabase.getDatabase(context).searchQueryDao()

    LaunchedEffect(Unit, searchQueryDao) {
        focusRequester.requestFocus()
        keyboardController?.show()
        searchQueryDao.observeRecentQueries(10).collect {
            searchQueries = it
        }
    }

    LaunchedEffect(Unit) {
        if (query != null) {
            searchQuery = TextFieldValue(query,
                selection = TextRange(query.length)
            )
        }
    }
    Column(
        Modifier.fillMaxSize(),
    )
    {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.search_hint),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            null
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.text.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = TextFieldValue("") },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.errorContainer
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            println("Search query: $searchQuery")
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            scope.launch {
                                searchQueryDao.deleteQuery(searchQuery.text.trim())
                                searchQueryDao.insertQuery(SearchQuery(query = searchQuery.text.trim()))
                            }
                            navController.navigate(SearchResultScreen(searchQuery.text.trim()))
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
//                    focusedTextColor = Color.White,
//                    unfocusedTextColor = Color.White,
//                    cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        if (searchQuery.text.isNotEmpty()){
            Box(Modifier.padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 4.dp)) {
                Text("Search Suggestions", style = MaterialTheme.typography.titleLarge)
            }
            var searchSuggestions by remember { mutableStateOf(emptyList<String>()) }

            LaunchedEffect(searchQuery) {
                searchSuggestions = searchSuggestions(searchQuery.text)
                    .flowOn(Dispatchers.IO)
                    .toList()
            }
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 0.dp)
                    .fillMaxWidth(),
            ) {
                items(
                    count = searchSuggestions.size
                ) { index ->
                    Row(Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp)
                        .clickable {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            scope.launch {
                                searchQueryDao.deleteQuery(searchSuggestions[index])
                                searchQueryDao.insertQuery(SearchQuery(query = searchSuggestions[index]))
                            }
                            navController.navigate(SearchResultScreen(searchSuggestions[index]))
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MarqueeBox(text = searchSuggestions[index],
                            boxModifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                        )
                        IconButton(onClick = {
                            scope.launch {
                                searchQuery = TextFieldValue(searchSuggestions[index],
                                    selection = TextRange(searchSuggestions[index].length)
                                )
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            }
        }

        if (searchQuery.text.isEmpty()) {
            Box(Modifier.padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)) {
                Text("Search History", style = MaterialTheme.typography.titleLarge)
            }
        } else {
            Box(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp)) {
                Text("Search History", style = MaterialTheme.typography.titleLarge)
            }
        }

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 0.dp)
                .fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(count = searchQueries.size) { index ->
                if (searchQueries[index].query != "") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                scope.launch {
                                    searchQueryDao.deleteQuery(searchQuery.text)
                                    searchQueryDao.insertQuery(SearchQuery(query = searchQuery.text))
                                }
                                navController.navigate(SearchResultScreen(searchQueries[index].query))
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        MarqueeBox(text = searchQueries[index].query,
                            boxModifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            )

                        IconButton(onClick = {
                            scope.launch {
                                searchQueryDao.deleteQuery(searchQueries[index].query)
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                        IconButton(onClick = {
                            scope.launch {
                                searchQuery = TextFieldValue(
                                    searchQueries[index].query,
                                    selection = TextRange(searchQueries[index].query.length)
                                )
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            }
        }
    }
}
