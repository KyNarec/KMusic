package com.kynarec.kmusic.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.kynarec.kmusic.data.repository.logs.LogsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class LogFileState(
    val wrapLines: Boolean = true,
    val showDeleteDialog: Boolean = false
)

sealed interface LogFileActions {
    data object ToggleWrapLines : LogFileActions
    data class ShowDeleteDialog(val file: File) : LogFileActions
    data object HideDeleteDialog : LogFileActions
    data class Delete(val file: File) : LogFileActions
    data class Share(val file: File, val context: Context) : LogFileActions
}

class LogFileViewModel(
    private val logsRepository: LogsRepository
) : ViewModel() {
    val tag = "LogsViewModel"
    private val _state = MutableStateFlow(LogFileState())
    val state: StateFlow<LogFileState> = _state.asStateFlow()

    fun onAction(action: LogFileActions) {
        when (action) {
            LogFileActions.ToggleWrapLines -> {
                _state.value = _state.value.copy(wrapLines = !state.value.wrapLines)
            }

            is LogFileActions.Delete -> {
                onAction(LogFileActions.HideDeleteDialog)
                logsRepository.deleteLog(action.file)
            }

            LogFileActions.HideDeleteDialog -> {
                _state.value = state.value.copy(showDeleteDialog = false)

            }

            is LogFileActions.Share -> {
                logsRepository.shareLogs(
                    file = action.file,
                    context = action.context
                )
            }

            is LogFileActions.ShowDeleteDialog -> {
                _state.value = state.value.copy(showDeleteDialog = true)
            }
        }
    }

}