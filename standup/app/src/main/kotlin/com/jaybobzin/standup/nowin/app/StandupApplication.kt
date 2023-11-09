/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.nowin.app

import android.app.Application
import com.jaybobzin.standup.integration.youtube.SuYtManager
import com.jaybobzin.standup.integration.youtube.SuYtManager.Config
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

private const val TAG = "StandupApplication"

@HiltAndroidApp
class StandupApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(SuTimberTree())
        }
    }
}

class SuTimberTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val fullTag = "su#$tag"
        // prefix all application tags with su# for easier filtering
        super.log(priority, fullTag, message, t)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideYtSecrets() : SuYtManager.Config {
        return object : Config {
            override val serverClientId: String = BuildConfig.google_server_client_id
            override val appName: String = BuildConfig.APPLICATION_ID
            override val apiKey: String = BuildConfig.google_api_key
        }
    }
}