/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.core

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaybobzin.standup.data.auth.AuthDataViewModel

private const val TAG = "StandupComponent"
internal object StandupComponent {
    data class Deps(val activity: StandupActivity)

    @Composable
    fun Content(deps: Deps) {
        val viewModel: StandupViewModel = hiltViewModel()
        val authViewModel: AuthDataViewModel = hiltViewModel()
        val countdownVal = viewModel.countdownFlow.collectAsStateWithLifecycle().value

        val tokens = authViewModel.tokensFlow.collectAsStateWithLifecycle().value

        LaunchedEffect(key1 = tokens) {
            viewModel.ytManager.fetchData()
        }
        val playlistList = viewModel.ytManager.playlistList.collectAsStateWithLifecycle().value

        LazyColumn {
            countdownVal?.let {
                item {
                    Text(if (it > 0) "$it" else "Stand\nUp!")
                }
                item {
                    if (tokens == null || tokens.isExpired() || playlistList == null) {
                        Button({ viewModel.loginGoogle(deps.activity) }) {
                            Text("Login Google")
                        }
                    } else {
                        Text("Found tokens")
                    }
                }
                if (playlistList == null) {
                    item { Text("Loading playlists") }
                    item { Text(text = "$tokens") }
                } else {
                    items(items = playlistList, key = { it.id }) { playlist ->
                        Text("${playlist.id} : ${playlist.kind} : ${playlist.snippet} \n $playlist")
                    }
                }
            }
        }
    }
}
