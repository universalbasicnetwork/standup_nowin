/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.nowin.app

import android.accounts.AccountManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaybobzin.standup.integration.youtube.SuForegroundService
import com.jaybobzin.standup.integration.youtube.SuForegroundServiceBinder
import com.jaybobzin.standup.nowin.app.StandupComponent.Deps
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber




private const val TAG = "StandupActivity"

@AndroidEntryPoint
class StandupActivity : ComponentActivity() {

    private val viewModel: StandupViewModel by viewModels()

    private var connection: YtConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(TAG).i("onCreate")

        setContent {
            StandupComponent.Content(Deps(this))
        }
//        viewModel.ytManager.getGoogleId(this)
//        viewModel.ytManager.signInWithGoogle(this)
//        viewModel.ytManager.authorize()


//        viewModel.mutableActivityFlow.value = this


    }

    override fun onDestroy() {
        super.onDestroy()
//        viewModel.mutableActivityFlow.value = null
    }

    override fun onStart() {
        super.onStart()
        this.connection = YtConnection(viewModel).also { connection ->
            Intent(this, SuForegroundService::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        this.connection?.let {
            unbindService(it)
            this.connection = null
        }
        viewModel.ytBound(null)
    }
}

class YtConnection(private val viewModel: StandupViewModel) : ServiceConnection {
    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance.
        viewModel.ytBound(service as SuForegroundServiceBinder)
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
        viewModel.ytBound(null)
    }
}
