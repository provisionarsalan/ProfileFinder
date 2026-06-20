package com.profilefinder.data.repository

import com.profilefinder.data.api.GitHubApi
import com.profilefinder.data.api.RedditApi
import com.profilefinder.data.db.SearchHistoryDao
import com.profilefinder.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val gitHubApi: GitHubApi,
    private val redditApi: RedditApi,
    private val searchHistoryDao: SearchHistoryDao
) {

    suspend fun searchByUsername(username: String): List<ProfileResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ProfileResult>()

        // GitHub
        runCatching {
            val gh = gitHubApi.getUser(username)
            val links = buildList {
                gh.blog?.takeIf { it.isNotBlank() }?.let { add(it) }
                gh.twitterUsername?.let { add("https://twitter.com/$it") }
            }
            results.add(
                ProfileResult(
                    platform = "GitHub",
                    username = gh.login,
                    displayName = gh.name,
                    bio = gh.bio,
                    avatarUrl = gh.avatarUrl,
                    profileUrl = gh.htmlUrl,
                    publicLinks = links,
                    publicStats = mapOf(
                        "Repos" to gh.publicRepos.toString(),
                        "Followers" to gh.followers.toString(),
                        "Following" to gh.following.toString()
                    ),
                    sourceNote = "GitHub Public API"
                )
            )
        }

        // Reddit
        runCatching {
            val rd = redditApi.getUser(username).data
            results.add(
                ProfileResult(
                    platform = "Reddit",
                    username = rd.name,
                    displayName = rd.name,
                    bio = rd.publicDescription,
                    avatarUrl = rd.iconImg?.takeIf { !it.contains("styles.redditmedia") },
                    profileUrl = "https://reddit.com/u/${rd.name}",
                    publicStats = mapOf(
                        "Link Karma" to rd.linkKarma.toString(),
                        "Comment Karma" to rd.commentKarma.toString()
                    ),
                    sourceNote = "Reddit Public API"
                )
            )
        }

        // Save to history
        if (results.isNotEmpty()) {
            searchHistoryDao.insert(
                SearchHistoryEntity(
                    query = username,
                    queryType = "USERNAME",
                    resultSummary = "${results.size} profiles found"
                )
            )
        }

        results
    }

    suspend fun searchByUrl(url: String): List<ProfileResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ProfileResult>()

        when {
            url.contains("github.com") -> {
                val username = url.substringAfter("github.com/").trim('/')
                if (username.isNotBlank()) {
                    runCatching {
                        val gh = gitHubApi.getUser(username)
                        results.add(
                            ProfileResult(
                                platform = "GitHub",
                                username = gh.login,
                                displayName = gh.name,
                                bio = gh.bio,
                                avatarUrl = gh.avatarUrl,
                                profileUrl = gh.htmlUrl,
                                publicLinks = listOfNotNull(gh.blog?.takeIf { it.isNotBlank() }),
                                publicStats = mapOf(
                                    "Repos" to gh.publicRepos.toString(),
                                    "Followers" to gh.followers.toString()
                                ),
                                sourceNote = "GitHub Public API"
                            )
                        )
                    }
                }
            }
            url.contains("reddit.com/u/") || url.contains("reddit.com/user/") -> {
                val username = url.substringAfterLast("/u/").substringAfterLast("/user/").trim('/')
                if (username.isNotBlank()) {
                    runCatching {
                        val rd = redditApi.getUser(username).data
                        results.add(
                            ProfileResult(
                                platform = "Reddit",
                                username = rd.name,
                                displayName = rd.name,
                                bio = rd.publicDescription,
                                avatarUrl = rd.iconImg,
                                profileUrl = "https://reddit.com/u/${rd.name}",
                                publicStats = mapOf(
                                    "Link Karma" to rd.linkKarma.toString(),
                                    "Comment Karma" to rd.commentKarma.toString()
                                ),
                                sourceNote = "Reddit Public API"
                            )
                        )
                    }
                }
            }
            else -> {
                // Generic website — just record the URL
                results.add(
                    ProfileResult(
                        platform = "Website",
                        username = url,
                        displayName = null,
                        bio = "Public website link",
                        avatarUrl = null,
                        profileUrl = url,
                        publicLinks = listOf(url),
                        sourceNote = "User-provided URL"
                    )
                )
            }
        }

        if (results.isNotEmpty()) {
            searchHistoryDao.insert(
                SearchHistoryEntity(
                    query = url,
                    queryType = "URL",
                    resultSummary = "${results.size} profiles found"
                )
            )
        }

        results
    }

    fun getSearchHistory() = searchHistoryDao.getAll()

    suspend fun clearHistory() = searchHistoryDao.clearAll()
}
