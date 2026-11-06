package com.monmoy.cmfessentialremapper.presentation.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepository
import com.monmoy.cmfessentialremapper.data.repository.RingerModeRepositoryImpl
import com.monmoy.cmfessentialremapper.domain.model.EssentialKeyStatus
import com.monmoy.cmfessentialremapper.domain.model.RingerMode
import com.monmoy.cmfessentialremapper.domain.usecase.CycleRingerModeUseCase
import com.monmoy.cmfessentialremapper.domain.usecase.GetEssentialKeyStatusUseCase
import com.monmoy.cmfessentialremapper.domain.usecase.GetRingerModeUseCase
import com.monmoy.cmfessentialremapper.domain.usecase.SetRingerModeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getRingerModeUseCase: GetRingerModeUseCase,
    private val setRingerModeUseCase: SetRingerModeUseCase,
    private val cycleRingerModeUseCase: CycleRingerModeUseCase,
    private val getEssentialKeyStatusUseCase: GetEssentialKeyStatusUseCase,
    private val repository: RingerModeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSystemState()
    }

    private fun observeSystemState() {
        viewModelScope.launch {
            combine(
                getRingerModeUseCase(),
                getEssentialKeyStatusUseCase()
            ) { ringerMode, keyStatus ->
                Pair(ringerMode, keyStatus)
            }
                .catch { exception ->
                    _uiState.update {
                        it.copy(errorMessage = exception.localizedMessage ?: "Audio error")
                    }
                }
                .collect { (ringerMode, keyStatus) ->
                    val isSetupComplete = keyStatus is EssentialKeyStatus.Ready && repository.hasNotificationPolicyAccess()
                    _uiState.update {
                        it.copy(
                            currentRingerMode = ringerMode,
                            keyStatus = keyStatus,
                            isSetupComplete = isSetupComplete
                        )
                    }
                }
        }
    }

    fun onSelectRingerMode(targetMode: RingerMode) {
        if (_uiState.value.isChangingMode || _uiState.value.currentRingerMode == targetMode) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingMode = true, errorMessage = null) }
            val result = setRingerModeUseCase(targetMode)
            result.fold(
                onSuccess = { actualMode ->
                    _uiState.update {
                        it.copy(currentRingerMode = actualMode, isChangingMode = false)
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            errorMessage = throwable.localizedMessage ?: "Failed to set ringer mode.",
                            isChangingMode = false
                        )
                    }
                }
            )
        }
    }

    fun onCycleMode() {
        if (_uiState.value.isChangingMode) return
        viewModelScope.launch {
            _uiState.update { it.copy(isChangingMode = true, errorMessage = null) }
            val result = cycleRingerModeUseCase()
            result.fold(
                onSuccess = { actualMode ->
                    _uiState.update {
                        it.copy(currentRingerMode = actualMode, isChangingMode = false)
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            errorMessage = throwable.localizedMessage ?: "Cycle failed.",
                            isChangingMode = false
                        )
                    }
                }
            )
        }
    }

    fun onAppResumed() {
        viewModelScope.launch {
            val current = repository.getCurrentRingerMode()
            val hasPolicy = repository.hasNotificationPolicyAccess()
            val isSetupComplete = _uiState.value.keyStatus is EssentialKeyStatus.Ready && hasPolicy
            _uiState.update {
                it.copy(
                    currentRingerMode = current,
                    isSetupComplete = isSetupComplete
                )
            }
        }
    }

    fun onErrorMessageDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onSettingsClicked() {
        _uiState.update { it.copy(isSettingsDialogOpen = true) }
    }

    fun onDismissSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogOpen = false) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = RingerModeRepositoryImpl(application.applicationContext)
            return HomeViewModel(
                getRingerModeUseCase = GetRingerModeUseCase(repository),
                setRingerModeUseCase = SetRingerModeUseCase(repository),
                cycleRingerModeUseCase = CycleRingerModeUseCase(repository),
                getEssentialKeyStatusUseCase = GetEssentialKeyStatusUseCase(repository),
                repository = repository
            ) as T
        }
    }
}
