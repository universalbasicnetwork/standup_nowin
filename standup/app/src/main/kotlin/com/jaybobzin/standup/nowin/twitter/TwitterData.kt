/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.nowin.twitter

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

object TwitterData {

    const val dbName = "TwitterDb"

    @Database(entities = [User::class, Connection::class], version = 1)
    abstract class Db : RoomDatabase() {
        abstract fun dao(): Dao
    }
    @androidx.room.Dao
    interface Dao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(user: User)

        @Insert
        fun insert(connection: Connection)

        @Query("SELECT * FROM user WHERE username = :username")
        fun user(username: String) : Flow<User?>

        @Query(
            "SELECT follower.username " +
            "FROM user following " +
            "JOIN connection conn ON conn.following_uid = following.uid " +
            "JOIN user follower ON conn.follower_uid = follower.uid " +
            "WHERE following.username = :username "
        )
        fun followers(username: String) : Flow<List<String>>
    }
    @Entity
    data class User (
        @PrimaryKey val uid: String,
        @ColumnInfo(name="username") val username: String
    )

    @Entity
    data class Connection(
        @ColumnInfo(name="follower_uid") val followerUid: String,
        @ColumnInfo(name="following_uid") val followingUid: String,
    )

}
