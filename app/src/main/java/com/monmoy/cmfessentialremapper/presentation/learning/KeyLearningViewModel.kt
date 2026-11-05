package com.monmoy.cmfessentialremapper.presentation.learning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monmoy.cmfessentialremapper.data.preferences.DataStoreManager
import com.monmoy.cmfessentialremapper.service.EssentialKeyAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KeyLearningUiState(
    val isListening: Boolean = false,
    val isServiceRunning: Boolean = false,
    val currentSavedKeyCode: Int = 0,
    val currentSavedScanCode: Int = 250,
    val detectedKeyCode: Int? = null,
    val detectedScanCode: Int? = null,
    val isKeySaved: Boolean = false,
    val message: String? = null
)

class KeyLearningViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application.applicationContext)

    private val _uiState = MutableStateFlow(KeyLearningUiState())
    val uiState: StateFlow<KeyLearningUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                EssentialKeyAccessibilityService.isServiceRunning.collect { running ->
                    _uiState.update { it.copy(isServiceRunning = running) }
                }
            }
            launch {
                dataStoreManager.learnedKeyCode.collect { code ->
                    _uiState.update { it.copy(currentSavedKeyCode = code) }
                }
            }
            launch {
                dataStoreManager.learnedScanCode.collect { scan ->
                    _uiState.update { it.copy(currentSavedScanCode = scan) }
                }
            }
        }
    }

    fun startListening() {
        _uiState.update {
            it.copy(
                isListening = true,
                detectedKeyCode = null,
                detectedScanCode = null,
                message = "Press your physical Essential Key now..."
            )
        }

        EssentialKeyAccessibilityService.activeKeyLearningListener = { keyCode, scanCode ->
            _uiState.update {
                it.copy(
                    isListening = false,
                    detectedKeyCode = keyCode,
                    detectedScanCode = scanCode,
                    message = "Key detected! Confirm to save this hardware identifier."
                )
            }
            // Deactivate listener once a key is captured
            EssentialKeyAccessibilityService.activeKeyLearningListener = null
        }
    }

    fun stopListening() {
        EssentialKeyAccessibilityService.activeKeyLearningListener = null
        _uiState.update { it.copy(isListening = false) }
    }

    fun saveDetectedKey() {
        val keyCode = _uiState.value.detectedKeyCode ?: return
        val scanCode = _uiState.value.detectedScanCode ?: return

        viewModelScope.launch {
            dataStoreManager.saveLearnedKey(keyCode, scanCode)
            _uiState.update {
                it.copy(
                    isKeySaved = true,
                    message = "Essential Key saved (Code: $keyCode, Scan: $scanCode)."
                )
            }
        }
    }

    fun retry() {
        startListening()
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            dataStoreManager.resetLearnedKey()
            _uiState.update {
                it.copy(
                    detectedKeyCode = null,
                    detectedScanCode = null,
                    isKeySaved = false,
                    message = "Reset to default CMF Phone 2 Pro reference (0/250)."
                )
            }
        }
    }

    override fun onCleared() {
        EssentialKeyAccessibilityService.activeKeyLearningListener = null
        super.onCleared()
    }
}

