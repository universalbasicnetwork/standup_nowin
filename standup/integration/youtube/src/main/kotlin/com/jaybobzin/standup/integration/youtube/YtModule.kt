/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.integration.youtube

import android.content.Context
import androidx.room.Room
import com.jaybobzin.standup.integration.youtube.YtData.PlaylistDao
import com.jaybobzin.standup.integration.youtube.YtData.YtDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DB_NAME = "YtDb"
@Module
@InstallIn(SingletonComponent::class)
class YtModule {
    @Provides
    @Singleton
    fun provideYtDb(
        @ApplicationContext context: Context
    ) :  YtDb = Room.databaseBuilder(
            context,
            YtDb::class.java,
            DB_NAME)
        .build()
    @Provides
    @Singleton
    fun providePlaylistDao(
        ytDb: YtDb
    ) : YtData.PlaylistDao = ytDb.playlistDao
}