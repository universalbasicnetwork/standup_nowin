/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

package com.jaybobzin.standup.nowin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.LazyColumn
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
        val user = viewModel.userFlow.collectAsStateWithLifecycle().value
        LazyColumn {
            countdownVal?.let {
                item(it) {
                    Text(if (it > 0) "$it" else "Stand\nUp!")
                }
            }
            user?.let {
                item(user.uid) {
                    Text("${it.username}(${it.uid}")
                }
            }
        }
    }
}

