/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.nowin.twitter

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.room.Room
import com.jaybobzin.standup.nowin.app.BuildConfig
import com.jaybobzin.standup.nowin.twitter.TwitterData.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import twitter4j.Twitter

private const val JAYBOBZIN = "jaybobzin"

class TwitterRepo(private val applicationContext: Context) {

    private val coroutineDispatcher : CoroutineDispatcher = Dispatchers.IO

    val db = Room.databaseBuilder(
        applicationContext,
        TwitterData.Db::class.java, TwitterData.dbName
    ).build()

    val api : Twitter = Twitter.newBuilder()
        .prettyDebugEnabled(BuildConfig.DEBUG)
        .oAuthConsumer(BuildConfig.twitter_api_key, BuildConfig.twitter_api_secret)
        .oAuthAccessToken(BuildConfig.twitter_access_token, BuildConfig.twitter_access_secret)
        .build()

//    val api = TwitterFactory(ConfigurationBuilder()
//        .setDebugEnabled(BuildConfig.DEBUG)
//        .setPrettyDebugEnabled(BuildConfig.DEBUG)
//        .setOAuthConsumerKey(BuildConfig.twitter_api_key)
//        .setOAuthConsumerSecret(BuildConfig.twitter_api_secret)
//        .setOAuthAccessToken(BuildConfig.twitter_access_token)
//        .setOAuthAccessTokenSecret(BuildConfig.twitter_access_secret)
//        .setJSONStoreEnabled(true)
//        .build())
//        .instance

    //    val apiInstance: TwitterApi = TwitterApi(TwitterCredentialsBearer(BuildConfig.twitter_bearer))

    suspend fun start() {
        coroutineScope {
            launch(coroutineDispatcher) {
                db.dao().user(JAYBOBZIN).collect {
                    if (it == null) {
                        val msg = "User not found"
                        warn(msg)
//                        val userResponse = apiInstance.users().findUserByUsername(JAYBOBZIN).execute()
//                        val userResponse = api.v1.getUsersBy(usernames = arrayOf(JAYBOBZIN))
                        val userResponse = api.v1().users().showUser(JAYBOBZIN)
                        val user = userResponse
                        if (user == null) {
                            warn("Data returned null")
                        } else {
                            db.dao().insert(convert(user))
                        }
                    }
                }
            }
        }
    }

    fun user() : Flow<User?> {
        return db.dao().user(JAYBOBZIN)
    }

    private fun convert(user: twitter4j.v1.User): TwitterData.User {
        return TwitterData.User(
            uid = user.id,
            username = user.screenName,
        )
    }

    private suspend fun warn(msg: String) {
        coroutineScope {
            launch(AndroidUiDispatcher.Main) {
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

}

//        try {
//            // findTweetById
//            Get2TweetsIdResponse result = apiInstance.tweets().findTweetById("20")
//                .tweetFields(tweetFields)
//                .execute();
//            if(result.getErrors() != null && result.getErrors().size() > 0) {
//                System.out.println("Error:");
//
//                result.getErrors().forEach(e -> {
//                    System.out.println(e.toString());
//                    if (e instanceof ResourceUnauthorizedProblem) {
//                        System.out.println(((ResourceUnauthorizedProblem) e).getTitle() + " " + ((ResourceUnauthorizedProblem) e).getDetail());
//                    }
//                });
//            } else {
//                System.out.println("findTweetById - Tweet Text: " + result.toString());
//            }
//        } catch (ApiException e) {
//            System.err.println("Status code: " + e.getCode());
//            System.err.println("Reason: " + e.getResponseBody());
//            System.err.println("Response headers: " + e.getResponseHeaders());
//            e.printStackTrace();
//        }
//    }
//    }
//}