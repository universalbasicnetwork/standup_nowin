/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.nowin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val TAG = "StandupActivity"

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
        countdownVal?.let {
            Text(if (it > 0) "$it" else "Stand\nUp!")
        }
    }
}
