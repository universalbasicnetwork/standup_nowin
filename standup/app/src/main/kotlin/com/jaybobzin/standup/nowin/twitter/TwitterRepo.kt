/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.nowin.twitter

import android.content.Context
import android.widget.Toast
import androidx.room.Room
import com.jaybobzin.standup.nowin.app.BuildConfig
import com.jaybobzin.standup.nowin.twitter.TwitterData.User
import com.twitter.clientlib.TwitterCredentialsBearer
import com.twitter.clientlib.api.TwitterApi
import kotlinx.coroutines.flow.Flow

private const val JAYBOBZIN = "jaybobzin"

class TwitterRepo(val applicationContext: Context) {

    val db = Room.databaseBuilder(
        applicationContext,
        TwitterData.Db::class.java, TwitterData.dbName
    ).build()

    val apiInstance: TwitterApi = TwitterApi(TwitterCredentialsBearer(BuildConfig.twitter_bearer));

    suspend fun start() {
        db.dao().user(JAYBOBZIN).collect {
            if (it == null) {
                val msg = "User not found"
                warn(msg)
                val userResponse = apiInstance.users().findUserByUsername(JAYBOBZIN).execute()
                val user = userResponse.data
                if (user == null) {
                    warn("Data returned null")
                } else {
                    db.dao().insert(convert(user))
                }
            }
        }
    }

    fun user() : Flow<User?> {
        return db.dao().user(JAYBOBZIN)
    }

    private fun convert(user: com.twitter.clientlib.model.User): TwitterData.User {
        return TwitterData.User(
            uid = user.id,
            username = user.username
        )
    }

    private fun warn(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
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