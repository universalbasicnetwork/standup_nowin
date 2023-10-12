/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.nowin.twitter

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import twitter4j.User2

typealias ApiUser = User2

object TwitterData {

    const val dbName = "TwitterDb"

    @Database(
        version = 2,
        entities = [User::class, Connection::class],
        exportSchema = true
    )

    abstract class Db : RoomDatabase() {
        abstract fun read(): ReadDao
        abstract fun write(): WriteDao
    }

    @Dao
    interface ReadDao {
        @Query("SELECT * FROM user WHERE username = :username")
        fun user(username: String): Flow<User?>

        @Query("SELECT * FROM user WHERE uid = :uid")
        fun user(uid: Long): Flow<User?>

        @Query(
            "SELECT * " +
                "FROM connection " +
                "WHERE following_uid = :uid " +
                "   OR follower_uid = :uid " +
                "ORDER BY updated_at ASC"
        )
        fun connections(uid: Long): Flow<List<Connection>>
    }

    @Dao
    interface WriteDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertUsers(users: Collection<User>)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertConnections(connections: Collection<Connection>)

        @Query("DELETE FROM connection WHERE follower_uid = :uid OR following_uid = :uid")
        fun deleteConnections(uid: Long) : Int
    }

    @Entity
    data class User (
        @PrimaryKey val uid: Long,
        @ColumnInfo(name="username") val username: String,
        @ColumnInfo(name="updated_at") val updatedAt: Long,
        @ColumnInfo(name="json") val json: String
    ) {
        companion object {
            fun from(gson: Gson, user: twitter4j.User, currentTime: Long) : User{
                return TwitterData.User(
                    uid = user.id,
                    username = user.screenName,
                    updatedAt = currentTime,
                    json = gson.toJson(user)

                )
            }
            fun from(gson: Gson, user: ApiUser, currentTime: Long) : User{
                return TwitterData.User(
                    uid = user.id,
                    username = user.screenName,
                    updatedAt = currentTime,
                    json = gson.toJson(user)

                )
            }
        }

        @Ignore
        private var t4jUser: ApiUser? = null
        fun user(gson: Gson) : ApiUser {
            val t4jUser = this.t4jUser ?: gson.fromJson(
                json,
                ApiUser::class.java,
            )
            if (this.t4jUser == null) this.t4jUser = t4jUser
            return t4jUser
        }
    }

    @Entity
    data class Connection(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        @ColumnInfo(name="follower_uid") val followerUid: Long,
        @ColumnInfo(name="following_uid") val followingUid: Long,
        @ColumnInfo(name="updated_at") val updatedAt: Long,
    )

}
