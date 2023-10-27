/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.nowin.app

import android.os.Bundle
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

    val ioScope = CoroutineScope(Dispatchers.IO)
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
            }
        }
    }
}
