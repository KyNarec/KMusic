package com.kynarec.kmusic.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.data.repository.logs.LogsRepository
import com.kynarec.kmusic.ui.components.settings.logs.LogTimespan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds

/**
 * @param savedLogs: List of saved logs and their respective expansion state
 */
data class LogsState(
    val filenameValue: String? = null,
    val filenamePlaceholder: String = "kmusic-logs-${
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
    }",
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val savedLogs: List<Pair<File, Boolean>> = emptyList(),
    val showDeleteDialog: Pair<Boolean, File> = false to File(""),
    val selectedTimespan: LogTimespan = LogTimespan.Minutes
)

sealed interface LogsActions {
    data class SetFilenameValue(val value: String) : LogsActions
    data object Capture : LogsActions
    data object Refresh : LogsActions
    data class ToggleExpansion(val index: Int) : LogsActions
    data class ShowDeleteDialog(val file: File) : LogsActions
    data object HideDeleteDialog : LogsActions
    data class Delete(val file: File) : LogsActions
    data class Share(val file: File, val context: Context) : LogsActions
    data class SetSelectedTimespan(val timespan: LogTimespan) : LogsActions
}

class LogsViewModel(
    private val logsRepository: LogsRepository
) : ViewModel() {
    val tag = "LogsViewModel"
    private val _state = MutableStateFlow(LogsState())
    val state: StateFlow<LogsState> = _state.asStateFlow()

    init {
        updateFilenamePlaceholder()
        getLogcatFiles()
        Log.i(tag, "LogsViewModel initialized")
    }

    fun onAction(logsActions: LogsActions) {
        when (logsActions) {
            is LogsActions.SetFilenameValue -> {
                _state.value = _state.value.copy(filenameValue = logsActions.value)
            }

            LogsActions.Capture -> {
                updateFilenamePlaceholder()
                val filename = "${
                    if (_state.value.filenameValue.isNullOrBlank()) 
                        _state.value.filenamePlaceholder 
                    else _state.value.filenameValue
                }.logcat"
                logsRepository.captureLogs(
                    filename = filename,
                    logTimespan = state.value.selectedTimespan
                )
                Log.i(tag, "Capturing logs and saving to $filename")
                getLogcatFiles()
            }

            LogsActions.Refresh -> {
                _state.value = state.value.copy(isRefreshing = true)
                viewModelScope.launch(Dispatchers.IO) {
                    getLogcatFiles()
                    delay(1000.milliseconds)
                    withContext(Dispatchers.Main) {
                        _state.value = state.value.copy(isRefreshing = false)
                    }
                }
            }

            is LogsActions.ToggleExpansion -> {
                _state.value = state.value.copy(savedLogs = state.value.savedLogs.mapIndexed { index, pair -> if (index == logsActions.index) pair.copy(second = !pair.second) else pair })
            }

            is LogsActions.ShowDeleteDialog -> {
                _state.value = state.value.copy(showDeleteDialog = true to logsActions.file)
            }
            LogsActions.HideDeleteDialog -> {
                _state.value = state.value.copy(showDeleteDialog = false to File(""))
            }

            is LogsActions.Delete -> {
                onAction(LogsActions.HideDeleteDialog)
                logsRepository.deleteLog(logsActions.file)
                getLogcatFiles()
            }

            is LogsActions.Share -> {
                logsRepository.shareLogs(
                    file = logsActions.file,
                    context = logsActions.context
                )
            }

            is LogsActions.SetSelectedTimespan -> {
                _state.value = state.value.copy(selectedTimespan = logsActions.timespan)
            }
        }
    }

    fun updateFilenamePlaceholder() {
        _state.value = _state.value.copy(
            filenamePlaceholder = "kmusic-logs-${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
            }"
        )
    }

    fun getLogcatFiles() {
        _state.value = _state.value.copy(savedLogs = logsRepository.getLogcatFiles())
    }
}