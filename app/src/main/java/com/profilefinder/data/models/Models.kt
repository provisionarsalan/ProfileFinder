package com.profilefinder.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// --- Room Entities ---

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val queryType: String, // USERNAME, URL, WEBSITE
    val timestamp: Long = System.currentTimeMillis(),
    val resultSummary: String? = null
)

@Entity(tableName = "analysis_results")
data class AnalysisResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUri: String,
    val detectedObjects: String, // JSON array
    val ocrText: String,
    val imageDescription: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- Domain Models ---

data class ProfileResult(
    val platform: String,
    val username: String,
    val displayName: String?,
    val bio: String?,
    val avatarUrl: String?,
    val profileUrl: String,
    val publicLinks: List<String> = emptyList(),
    val publicStats: Map<String, String> = emptyMap(),
    val verifiedBadge: Boolean = false,
    val sourceNote: String = "Public API"
)

data class ImageAnalysisResult(
    val detectedObjects: List<DetectedObject>,
    val ocrText: String,
    val description: String,
    val confidence: Float
)

data class DetectedObject(
    val label: String,
    val confidence: Float
)

data class SearchQuery(
    val type: QueryType,
    val value: String
)

enum class QueryType { USERNAME, PROFILE_URL, WEBSITE }

// --- API Response Models ---

data class GitHubUser(
    val login: String,
    val name: String?,
    val bio: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("html_url") val htmlUrl: String,
    val blog: String?,
    val company: String?,
    val location: String?,
    val followers: Int,
    val following: Int,
    @SerializedName("public_repos") val publicRepos: Int,
    @SerializedName("twitter_username") val twitterUsername: String?
)

data class RedditUser(
    val data: RedditUserData
)

data class RedditUserData(
    val name: String,
    @SerializedName("icon_img") val iconImg: String?,
    @SerializedName("public_description") val publicDescription: String?,
    @SerializedName("link_karma") val linkKarma: Int,
    @SerializedName("comment_karma") val commentKarma: Int,
    @SerializedName("is_gold") val isGold: Boolean,
    @SerializedName("created_utc") val createdUtc: Long
)

data class AnalysisState(
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val currentStep: String = "",
    val result: ImageAnalysisResult? = null,
    val error: String? = null
)

data class ProfileSearchState(
    val isLoading: Boolean = false,
    val results: List<ProfileResult> = emptyList(),
    val error: String? = null,
    val query: String = ""
)

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
