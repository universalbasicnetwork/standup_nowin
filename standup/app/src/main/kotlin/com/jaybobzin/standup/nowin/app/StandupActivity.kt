/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

package com.jaybobzin.standup.nowin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "StandupActivity"

@AndroidEntryPoint
class StandupActivity : ComponentActivity() {

    val viewModel: StandupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ActivityComposable()
        }
    }

    @Composable
    private fun ActivityComposable() {
        val countdownVal = viewModel.countdownFlow.collectAsStateWithLifecycle().value
        val user = viewModel.user(JAYBOBZIN).collectAsStateWithLifecycle().value
        val connections = user?.let { viewModel.connections(it.uid).value }
        LazyColumn {
            countdownVal?.let {
                item(it) {
                    Text(if (it > 0) "$it" else "Stand\nUp!")
                }
            }
            user?.let {user ->
                item(user.uid) {
                    Text("${user.username}(${user.uid}")
                }
                connections?.let {conns ->
                    items(conns, {it.id}) {conn ->
                        val follower = if (conn.followerUid == user.uid) user else
                            viewModel.user(conn.followerUid).collectAsStateWithLifecycle().value
                        val following = if (conn.followingUid == user.uid) user else
                            viewModel.user(conn.followingUid).collectAsStateWithLifecycle().value

                        Text(text = "${following?.username ?: conn.followingUid} <= ${follower?.username ?: conn.followerUid}")
                    }
                }
            }

        }
    }
}

