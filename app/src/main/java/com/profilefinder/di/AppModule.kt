package com.profilefinder.di

import android.content.Context
import androidx.room.Room
import com.profilefinder.data.api.GitHubApi
import com.profilefinder.data.api.RedditApi
import com.profilefinder.data.db.AnalysisResultDao
import com.profilefinder.data.db.ProfileFinderDatabase
import com.profilefinder.data.db.SearchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("User-Agent", "ProfileFinder/1.0")
                .build()
            chain.proceed(req)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("github")
    fun provideGitHubRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("reddit")
    fun provideRedditRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://www.reddit.com/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideGitHubApi(@Named("github") retrofit: Retrofit): GitHubApi =
        retrofit.create(GitHubApi::class.java)

    @Provides
    @Singleton
    fun provideRedditApi(@Named("reddit") retrofit: Retrofit): RedditApi =
        retrofit.create(RedditApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ProfileFinderDatabase =
        Room.databaseBuilder(context, ProfileFinderDatabase::class.java, "profile_finder_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSearchHistoryDao(db: ProfileFinderDatabase): SearchHistoryDao =
        db.searchHistoryDao()

    @Provides
    fun provideAnalysisResultDao(db: ProfileFinderDatabase): AnalysisResultDao =
        db.analysisResultDao()
}
