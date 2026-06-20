package com.profilefinder.data.api

import com.profilefinder.data.models.GitHubUser
import com.profilefinder.data.models.RedditUser
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {
    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): GitHubUser
}

interface RedditApi {
    @GET("user/{username}/about.json")
    suspend fun getUser(@Path("username") username: String): RedditUser
}
