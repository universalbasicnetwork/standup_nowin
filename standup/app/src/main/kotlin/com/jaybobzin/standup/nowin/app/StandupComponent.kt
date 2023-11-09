/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.nowin.app

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
        val googleId = viewModel.ytManager.googleIdToken.value

        val tokens = authViewModel.tokensFlow.collectAsStateWithLifecycle().value

        LazyColumn {
            countdownVal?.let {
                item {
                    Text(if (it > 0) "$it" else "Stand\nUp!")
                }
                item {
                    if (tokens == null || tokens.isExpired()) {
                        Button({viewModel.loginGoogle(deps.activity)}) {
                            Text("Login Google")
                        }
                    } else {
                        Text("Found tokens")
                    }
                }
                item {
                    Text(
                        when (viewModel.ytManager.success.value) {
                            0 -> "Loading"
                            1 -> "Success"
                            -1 -> "Failure"
                            else -> "huh? ${viewModel.ytManager.success.value}"
                        },
                    )
                }

                item {
                    googleId?.let {
                        Text(it.toString())
                    }
                }
            }
        }
    }
    @Composable
    fun YtContent() {
        val viewModel: StandupViewModel = hiltViewModel()
        val countdownVal = viewModel.countdownFlow.collectAsStateWithLifecycle().value
        val googleLogin = viewModel.ytManager.googleLogin.collectAsStateWithLifecycle().value
        LaunchedEffect(key1 = googleLogin) {
            viewModel.ytManager.fetchData()
        }

        val accounts = viewModel.accountsFlow.collectAsStateWithLifecycle().value

        val playlistList = viewModel.ytManager.playlistList.collectAsStateWithLifecycle().value
        LazyColumn {
            if (googleLogin == null) {
                countdownVal?.let {
                    item {
                        Text(if (it > 0) "$it" else "Stand\nUp!")
                    }
                }
            } else {
                item {
                    Text("Stand up ${googleLogin.displayName}!")
                }
            }
            if (playlistList == null) {
                item { Text("Loading playlists, showing accounts:") }
                accounts?.let {
                    items(items = accounts, key = { it.name}) {
                        Text("${it.type}: ${it.name}")
                    }
                }

            } else {
                items(items = playlistList, key = {it.id}) {playlist ->
                    Text("${playlist.id} : ${playlist.kind} : ${playlist.snippet} \n ${playlist.toString()}" )
                }
            }

            item {
                Text("========\nLogin details: $googleLogin")
            }
        }
    }
}