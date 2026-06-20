package com.profilefinder.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profilefinder.data.models.*
import com.profilefinder.data.repository.ImageAnalysisRepository
import com.profilefinder.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageAnalysisViewModel @Inject constructor(
    private val imageAnalysisRepository: ImageAnalysisRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state.asStateFlow()

    fun analyzeImage(uri: Uri) {
        viewModelScope.launch {
            _state.value = AnalysisState(isLoading = true, progress = 0f, currentStep = "Preparing image...")

            // Simulate analysis steps for UX
            val steps = listOf(
                0.15f to "Loading image...",
                0.30f to "Detecting objects...",
                0.55f to "Reading text (OCR)...",
                0.75f to "Analyzing features...",
                0.90f to "Generating summary..."
            )

            for ((progress, step) in steps) {
                _state.value = _state.value.copy(progress = progress, currentStep = step)
                delay(600L)
            }

            runCatching {
                imageAnalysisRepository.analyzeImage(uri)
            }.onSuccess { result ->
                _state.value = AnalysisState(
                    isLoading = false,
                    progress = 1f,
                    currentStep = "Done",
                    result = result
                )
            }.onFailure { e ->
                _state.value = AnalysisState(
                    isLoading = false,
                    error = e.message ?: "Analysis failed"
                )
            }
        }
    }

    fun reset() {
        _state.value = AnalysisState()
    }
}

@HiltViewModel
class ProfileSearchViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileSearchState())
    val state: StateFlow<ProfileSearchState> = _state.asStateFlow()

    val searchHistory = profileRepository.getSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun searchByUsername(username: String) {
        if (username.isBlank()) return
        viewModelScope.launch {
            _state.value = ProfileSearchState(isLoading = true, query = username)
            runCatching { profileRepository.searchByUsername(username.trim()) }
                .onSuccess { results ->
                    _state.value = ProfileSearchState(
                        results = results,
                        query = username,
                        error = if (results.isEmpty()) "No public profiles found for \"$username\"" else null
                    )
                }
                .onFailure { e ->
                    _state.value = ProfileSearchState(
                        error = "Search failed: ${e.message}",
                        query = username
                    )
                }
        }
    }

    fun searchByUrl(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _state.value = ProfileSearchState(isLoading = true, query = url)
            runCatching { profileRepository.searchByUrl(url.trim()) }
                .onSuccess { results ->
                    _state.value = ProfileSearchState(
                        results = results,
                        query = url,
                        error = if (results.isEmpty()) "No public profile found for this URL" else null
                    )
                }
                .onFailure { e ->
                    _state.value = ProfileSearchState(
                        error = "Fetch failed: ${e.message}",
                        query = url
                    )
                }
        }
    }

    fun clearHistory() {
        viewModelScope.launch { profileRepository.clearHistory() }
    }

    fun reset() {
        _state.value = ProfileSearchState()
    }
}
