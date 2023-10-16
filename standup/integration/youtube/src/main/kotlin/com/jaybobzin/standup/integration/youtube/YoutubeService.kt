/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.integration.youtube

import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

private const val TAG = "YoutubeService"

object YoutubeService {
    interface YtBinder {
        fun countdown(it: Int)

        val countdownFlow: Flow<Int?>
    }
    class YtServiceImpl : Service() {

        private val binder = LocalBinder()

        inner class LocalBinder : Binder(), YtBinder {
            override val countdownFlow: Flow<Int?> = flow {
                for (i in 3 downTo 0) {
                    emit(i)
                    delay(2000)  // delay for 2 seconds
                }
            }

            override fun countdown(it: Int) {
                Timber.tag(TAG).i("Yt Countdown $it")
            }
        }

        override fun onBind(intent: Intent?): IBinder {
            Timber.tag(TAG).i("onBind($intent)")
            return binder
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            Timber.tag(TAG).i("onStartCommand($intent, $flags, $startId)")
            val pendingIntent = getLauncherActivityPendingIntent()

            val channelId = "standup_notifications"
            val notificationTitle = "StandUp! Notif"
            val input = "Input text"
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle(notificationTitle)
                .setContentText(input)
                .setContentIntent(pendingIntent)
                .build()
            startForeground(1, notification)

            return START_STICKY_COMPATIBILITY
        }

        // This method is part of your Service or any other component where you're creating the notification.
        private fun getLauncherActivityPendingIntent(): PendingIntent? {

            // Create an intent that looks for the main launcher activity.
            val launcherActivityIntent = Intent(Intent.ACTION_MAIN)
            launcherActivityIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            launcherActivityIntent.setPackage(packageName)
            launcherActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Find the main launcher activity and make sure it's found.
            val pm = packageManager
            val resolveInfo =
                pm.resolveActivity(launcherActivityIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    ?: // No suitable activity found. Handle the error appropriately.
                    return null

            // Set the specific target component (this will be the launcher activity)
            launcherActivityIntent.component =
                ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)

            // Create a PendingIntent to be triggered when the notification is clicked
            return PendingIntent.getActivity(
                this,
                0,
                launcherActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }
    }
}