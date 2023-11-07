/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.nowin.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

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
