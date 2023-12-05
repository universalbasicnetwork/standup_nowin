/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.integration.youtube

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

object YtData {
    const val TABLE_PLAYLIST = "playlist"
    @Entity(tableName = TABLE_PLAYLIST)
    data class Playlist(
        @PrimaryKey val id: String,
        val channelTitle: String,
        val title: String,
        val description: String,
        val thumbnailUrl: String,
        val publishedAt: Long,
    )
    @Dao
    interface PlaylistDao {
        @Query("SELECT * FROM $TABLE_PLAYLIST ORDER BY publishedAt ASC")
        fun getPlaylists(): Flow<List<Playlist>>

        @Query("SELECT * FROM $TABLE_PLAYLIST WHERE id = :id")
        suspend fun getPlaylist(id: Int): Playlist

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun addPlaylist(playlist: Collection<Playlist>)

        @Query("DELETE FROM $TABLE_PLAYLIST")
        suspend fun deletePlaylists()
    }

    @Database(
        entities = [Playlist::class],
        version = 1,
        exportSchema = true
    )

    abstract class YtDb : RoomDatabase() {
        abstract val playlistDao: PlaylistDao
    }

    object Transformer{
        fun transform(items: List<com.google.api.services.youtube.model.Playlist>): List<Playlist> {
            return items.map {
                Playlist(
                    id = it.id,
                    channelTitle = it.snippet.channelTitle,
                    title = it.snippet.title,
                    description = it.snippet.description,
                    thumbnailUrl = it.snippet.thumbnails.default.url,
                    publishedAt = it.snippet.publishedAt.value
                )
            }
        }
    }
}