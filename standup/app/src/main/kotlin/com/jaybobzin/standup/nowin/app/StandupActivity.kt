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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.jaybobzin.standup.integration.youtube.SuForegroundService
import com.jaybobzin.standup.integration.youtube.SuForegroundServiceBinder
import com.jaybobzin.standup.integration.youtube.Youtube
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

private const val TAG = "StandupActivity"

@AndroidEntryPoint
class StandupActivity : ComponentActivity() {

    private val viewModel: StandupViewModel by viewModels()

    private lateinit var credentialManager: CredentialManager

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var connection : YtConnection? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(TAG).i("onCreate")
        credentialManager = CredentialManager.create(this)

        setContent {
            ActivityComponent.Content()
        }

        val googleIdOption: GetGoogleIdOption = Builder()
            .setFilterByAuthorizedAccounts(true)
            .setAutoSelectEnabled(true)
            .setServerClientId(BuildConfig.google_server_client_id)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        ioScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@StandupActivity,
                    request = request,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Timber.tag(TAG).e(e, "Failed to get credential")
                viewModel.success.value = -1
            }
        }
    }

    fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is PublicKeyCredential -> {
                // Share responseJson such as a GetCredentialResponse on your server to
                // validate and authenticate
                val responseJson = credential.authenticationResponseJson
                Timber.tag(TAG).i("Got response json: %s", responseJson)
                viewModel.success.value = 3
            }

            is PasswordCredential -> {
                // Send ID and password to your server to validate and authenticate.
                val username = credential.id
                val password = credential.password
                Timber.tag(TAG).i("Got un $username and pw(${password.length}")
                viewModel.success.value = 2
            }

            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        Timber.tag(TAG).i("Got googleIdToken $googleIdTokenCredential")
                        viewModel.success.value = 1
                        viewModel.googleIdToken.value = Youtube.Login.from(googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        Timber.tag(TAG).e(e, "Received an invalid google id token response")
                        viewModel.success.value = -1
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Timber.tag(TAG).e("Unexpected type of credential")
                    viewModel.success.value = -1
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Timber.tag(TAG).e("Unexpected type of credential")
                viewModel.success.value = -1
            }
        }
    }
    override fun onStart() {
        super.onStart()
        val connection = YtConnection(viewModel)
        this.connection = connection
        Intent(this, SuForegroundService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
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

private object ActivityComponent {
    @Composable
    fun Content() {
        val viewModel: StandupViewModel = hiltViewModel()
        val countdownVal = viewModel.countdownFlow.collectAsStateWithLifecycle().value
        val googleId = viewModel.googleIdToken.value
        LazyColumn {
            countdownVal?.let {
                item {
                    Text(if (it > 0) "$it" else "Stand\nUp!")
                }
                item {
                    Text(
                        when (viewModel.success.value) {
                            0 -> "Loading"
                            1 -> "Success"
                            -1 -> "Failure"
                            else -> "huh? ${viewModel.success.value}"
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
}

class YtConnection(private val viewModel : StandupViewModel) : ServiceConnection {
    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance.
        viewModel.ytBound(service as SuForegroundServiceBinder)
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
        viewModel.ytBound(null)
    }
}
