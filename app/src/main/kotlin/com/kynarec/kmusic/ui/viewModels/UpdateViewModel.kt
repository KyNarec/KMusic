package com.kynarec.kmusic.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynarec.kmusic.data.db.entities.GitHubRelease
import com.kynarec.kmusic.data.repository.UpdateRepository
import com.kynarec.kmusic.data.repository.UpdateResult
import com.kynarec.kmusic.enums.ReleaseNotificationType
import com.kynarec.kmusic.service.update.AppVersionProvider
import com.kynarec.kmusic.service.update.DownloadStatus
import com.kynarec.kmusic.service.update.UpdateInfo
import com.kynarec.kmusic.utils.Constants.DEFAULT_RELEASE_NOTIFICATION
import com.kynarec.kmusic.utils.Constants.DEFAULT_SHOW_PRE_RELEASES
import com.kynarec.kmusic.utils.Constants.RELEASE_NOTIFICATION_KEY
import com.kynarec.kmusic.utils.Constants.SHOW_PRE_RELEASES_KEY
import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UpdateState(
    val releases: List<GitHubRelease> = emptyList(),
    val fetchingError: String? = null,
    val fetchingLoadingState: Float = 0f,
    val updateInfo: UpdateInfo? = null,
    val downloadStatus: Map<Int, DownloadStatus> = emptyMap(),
    val downloadErrorDialog: String? = null,
    val currentInstalledVersion: String,
    val releaseNotificationType: ReleaseNotificationType = DEFAULT_RELEASE_NOTIFICATION,
    val showPreReleases: Boolean = DEFAULT_SHOW_PRE_RELEASES
)

sealed interface UpdateAction {
    data object Refresh : UpdateAction
    data object CloseErrorDialog : UpdateAction
    data class Download(val release: GitHubRelease, val context: Context) : UpdateAction
    data class DownloadErrorDialog(val message: String?) : UpdateAction
}

class UpdateViewModel(
    private val updateRepository: UpdateRepository,
    appVersionProvider: AppVersionProvider,
    kSafe: KSafe
) : ViewModel() {
    private val tag = "UpdateViewModel"
    private val _state =
        MutableStateFlow(UpdateState(currentInstalledVersion = appVersionProvider.currentVersion))

    val state: StateFlow<UpdateState> = combine(
        _state,
        kSafe.getFlow(RELEASE_NOTIFICATION_KEY, DEFAULT_RELEASE_NOTIFICATION),
        kSafe.getFlow(SHOW_PRE_RELEASES_KEY, DEFAULT_SHOW_PRE_RELEASES)
    ) { state, releaseNotificationType, showPreReleases ->
        state.copy(
            releaseNotificationType = releaseNotificationType,
            showPreReleases = showPreReleases
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UpdateState(
            currentInstalledVersion = appVersionProvider.currentVersion,
            showPreReleases = kSafe.getDirect(SHOW_PRE_RELEASES_KEY, DEFAULT_SHOW_PRE_RELEASES)
        )
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val releases = updateRepository.fetchDBReleases()
            withContext(Dispatchers.Main) {
                _state.value = _state.value.copy(
                    releases = releases
                )
            }
            refresh(silentError = true)
        }
    }

    fun onAction(action: UpdateAction) {
        when (action) {
            is UpdateAction.Refresh -> {
                viewModelScope.launch(Dispatchers.IO) {
                    refresh()
                }
            }

            is UpdateAction.CloseErrorDialog -> {
                _state.value = _state.value.copy(
                    fetchingError = null
                )
            }

            is UpdateAction.Download -> {
                viewModelScope.launch(Dispatchers.IO) {
                    updateRepository.downloadAndInstall(
                        release = action.release,
                        context = action.context
                    ).collect { downloadStatus ->
                        when (downloadStatus) {
                            is DownloadStatus.Completed -> {
                                withContext(Dispatchers.Main) {
//                                    val releaseId: Int = action.release.id

                                    _state.value = _state.value.copy(
                                        downloadStatus = _state.value.downloadStatus + (action.release.id to downloadStatus)
//                                        downloadStatus = _state.value.downloadStatus.mapValues { (id, status) ->
//                                            if (id == releaseId) downloadStatus else status
//                                        }
                                    )
                                }
                            }

                            is DownloadStatus.Error -> {
                                withContext(Dispatchers.Main) {
                                    _state.value = _state.value.copy(
                                        downloadStatus = _state.value.downloadStatus + (action.release.id to downloadStatus)
                                    )
                                }
                            }

                            DownloadStatus.NotStarted -> {}
                            is DownloadStatus.Progress -> {
                                withContext(Dispatchers.Main) {
                                    _state.value = _state.value.copy(
                                        downloadStatus = _state.value.downloadStatus + (action.release.id to downloadStatus)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is UpdateAction.DownloadErrorDialog -> {
                _state.value = _state.value.copy(
                    downloadErrorDialog = action.message
                )
            }
        }
    }

    suspend fun refresh(silentError: Boolean = false) {
        updateRepository.fetchReleases().collect {
            when (it) {
                is UpdateResult.Error -> {
                    Log.e(tag, "Error fetching releases: ${it.message}")
                    if (!silentError) withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(
                            fetchingError = it.message,
                            fetchingLoadingState = 0f
                        )
                    }
                }

                is UpdateResult.Loading -> {
                    Log.i(tag, "Loading releases: ${it.progress}")
                    if (!silentError) withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(
                            fetchingError = null,
                            fetchingLoadingState = it.progress

                        )
                    }
                }

                is UpdateResult.Success<*> -> {
                    Log.i(tag, "Received releases")
                    withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(
                            releases = it.data as List<GitHubRelease>,
                            fetchingError = null,
                            fetchingLoadingState = 0f
                        )
                    }
                }
            }
        }
    }
}