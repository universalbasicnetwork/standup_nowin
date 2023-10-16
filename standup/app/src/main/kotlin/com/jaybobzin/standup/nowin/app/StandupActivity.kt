/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.nowin.app

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jaybobzin.standup.integration.youtube.YoutubeService.YtBinder
import com.jaybobzin.standup.integration.youtube.YoutubeService.YtServiceImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

private const val TAG = "StandupActivity"

@AndroidEntryPoint
class StandupActivity : ComponentActivity() {

    companion object {
        const val SIGN_IN_REQUEST_CODE = 1001
    }

    val viewModel: StandupViewModel by viewModels()


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            viewModel.ytBound(service as YtBinder)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            viewModel.ytBound(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(TAG).i("onCreate($savedInstanceState)")
        setContent {
            ActivityComponent.Content()
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, YtServiceImpl::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        viewModel.ytBound(null)
    }
    /*
   * Checks whether the user is signed in. If so, executes the specified
   * function. If the user is not signed in, initiates the sign-in flow,
   * specifying the function to execute after the user signs in.
   */
    private fun fitSignIn() {
        if (oAuthPermissionsApproved()) {
            readDailySteps()
        } else {
            GoogleSignIn.requestPermissions(
                this,
                SIGN_IN_REQUEST_CODE,
                getGoogleAccount(),
                fitnessOptions
            )
        }
    }

    private fun oAuthPermissionsApproved() =
        GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)

    /*
     * Gets a Google account for use in creating the fitness client. This is
     * achieved by either using the last signed-in account, or if necessary,
     * prompting the user to sign in. It's better to use the
     * getAccountForExtension() method instead of the getLastSignedInAccount()
     * method because the latter can return null if there has been no sign in
     * before.
     */
    private fun getGoogleAccount(): GoogleSignInAccount =
        GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)

    /*
     * Handles the callback from the OAuth sign in flow, executing the function
     * after sign-in is complete.
     */
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            RESULT_OK -> {
                readDailySteps()
            }
            else -> {
                // Handle error.
            }
        }
    }

    /*
     * Reads the current daily step total.
     */
    private fun readDailySteps() {
        Fitness.getHistoryClient(requireContext(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first()
                        .getValue(Field.FIELD_STEPS).asInt()
                }

                Log.i(TAG, "Total steps: $total")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
    }

}
private object ActivityComponent {

    @Composable
    fun Content() {
        val viewModel: StandupViewModel = hiltViewModel()
        val countdownVal = viewModel.countdownFlow.collectAsStateWithLifecycle().value
        val ytBinder = viewModel.ytBinder.collectAsStateWithLifecycle().value

        LazyColumn {
            countdownVal?.let {
                ytBinder?.countdown(it)
                item(it) {
                    Text(if (it > 0) "$it" else "Stand\nUp!")
                }
            }
        }
    }
}
