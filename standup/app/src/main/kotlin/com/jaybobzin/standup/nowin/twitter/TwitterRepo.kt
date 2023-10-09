/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.nowin.twitter

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.room.Room
import com.google.gson.Gson
import com.jaybobzin.standup.nowin.app.BuildConfig
import com.jaybobzin.standup.nowin.twitter.TwitterData.Connection
import com.jaybobzin.standup.nowin.twitter.TwitterData.ReadDao
import com.jaybobzin.standup.nowin.twitter.TwitterData.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import twitter4j.Twitter
import twitter4j.v1.PagableResponseList

private const val PROFILE_UPDATE_MILLIS = 1000 * 60 * 60 * 24
private const val CONNECTION_UPDATE_MILLIS = 1000 * 60 * 60 * 24

class TwitterRepo(private val applicationContext: Context) {

    private val gson = Gson()
    private val coroutineDispatcher : CoroutineDispatcher = Dispatchers.IO

    private val db = Room.databaseBuilder(
        applicationContext,
        TwitterData.Db::class.java, TwitterData.dbName )
        .fallbackToDestructiveMigration()
        .build()

    private val api : Twitter = Twitter.newBuilder()
        .prettyDebugEnabled(BuildConfig.DEBUG)
        .oAuthConsumer(BuildConfig.twitter_api_key, BuildConfig.twitter_api_secret)
        .oAuthAccessToken(BuildConfig.twitter_access_token, BuildConfig.twitter_access_secret)
        .build()

    suspend fun start(defaultUsername: String) {
        fetchUser(defaultUsername)
    }

    private suspend fun fetchUser(username: String, force: Boolean = false) {
        coroutineScope {
            val readDao = db.read()
            val writeDao = db.write()

            launch(coroutineDispatcher) {
                val currentTime = System.currentTimeMillis()
                readDao.user(username).collect { existingUser ->
                    if (force
                        || existingUser == null
                        || currentTime - existingUser.updatedAt < PROFILE_UPDATE_MILLIS
                    ) {
                        warn("Fetching user: $username")
                        val t4jUser = api.v1().users().showUser(username)
                        if (t4jUser == null) {
                            warn("Data returned null for $username")
                        } else {
                            val fetchedUser = User.from(gson, t4jUser, currentTime)
                            writeDao.insertUsers(listOf(fetchedUser))
                            // when inserting a new user, always update the connections
                            fetchConnections(fetchedUser, true)
                        }
                    } else {
                        fetchConnections(existingUser)
                    }
                }
            }
        }
    }

    fun dao() : ReadDao {
        return db.read()
    }

    private suspend fun fetchConnections(user: User, force: Boolean = false) {
        coroutineScope {
            launch(coroutineDispatcher) {
                val currentTime = System.currentTimeMillis()
                val readDao = db.read()
                val writeDao = db.write()

                readDao.connections(user.uid).collect { existingConnections ->
                    val first = existingConnections.firstOrNull()
                    if (!force
                        && first != null
                        && currentTime - first.updatedAt <= CONNECTION_UPDATE_MILLIS) {
                        return@collect
                    }
                    // Start the delete but don't block on it yet
                    val deleted = async { writeDao.deleteConnections(user.uid) }
                    val users : MutableSet<User> = mutableSetOf()
                    val connections: MutableSet<Connection> = mutableSetOf()

                    var followers : PagableResponseList<twitter4j.v1.User>? =null
                    while (followers == null || followers.hasNext()) {
                        followers = api.v1().friendsFollowers().getFollowersList(user.uid,
                            followers?.nextCursor ?: 0, 200)


                        followers?.forEach {follower ->
                            users.add(User.from(gson, follower, currentTime))
                            connections.add(Connection(
                                followerUid = follower.id,
                                followingUid = user.uid,
                                updatedAt = currentTime
                            ))
                        }
                    }

                    // Insert everything at once so that it (more likely) all succeeds or fails
                    writeDao.insertUsers(users)
                    //Don't block on the delete until we are ready to insert new connections.
                    deleted.await()
                    writeDao.insertConnections(connections)
                }
            }
        }

    }

    private suspend fun warn(msg: String) {
        Log.w("TwitterRepo", msg)
        coroutineScope {
            launch(AndroidUiDispatcher.Main) {
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

}