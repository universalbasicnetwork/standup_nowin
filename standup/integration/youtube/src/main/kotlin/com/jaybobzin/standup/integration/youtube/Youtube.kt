/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.integration.youtube

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "Youtube"
object Youtube {
    class Manager @Inject constructor(@ApplicationContext applicationContext: Context) {
        private val ioScope = CoroutineScope(Dispatchers.IO)

        private lateinit var credentialManager: CredentialManager

        val googleIdToken: MutableState<Login?> = mutableStateOf(null)
        val success: MutableState<Int> = mutableIntStateOf(0)

        fun loginGoogle(context: Context, googleServerId: String) {
            credentialManager = CredentialManager.create(context)

            val googleIdOption: GetGoogleIdOption = Builder()
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .setServerClientId(googleServerId)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            ioScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        context = context,
                        request = request,
                    )
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    Timber.tag(TAG).e(e, "Failed to get credential")
                    success.value = -1
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
                    success.value = 3
                }

                is PasswordCredential -> {
                    // Send ID and password to your server to validate and authenticate.
                    val username = credential.id
                    val password = credential.password
                    Timber.tag(TAG).i("Got un $username and pw(${password.length}")
                    success.value = 2
                }

                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            // Use googleIdTokenCredential and extract id to validate and
                            // authenticate on your server.
                            val googleIdTokenCredential =
                                GoogleIdTokenCredential.createFrom(credential.data)
                            Timber.tag(TAG).i("Got googleIdToken $googleIdTokenCredential")
                            success.value = 1
                            googleIdToken.value = Login.from(googleIdTokenCredential)
                        } catch (e: GoogleIdTokenParsingException) {
                            Timber.tag(TAG).e(e, "Received an invalid google id token response")
                            success.value = -1
                        }
                    } else {
                        // Catch any unrecognized custom credential type here.
                        Timber.tag(TAG).e("Unexpected type of credential")
                        success.value = -1
                    }
                }

                else -> {
                    // Catch any unrecognized credential type here.
                    Timber.tag(TAG).e("Unexpected type of credential")
                    success.value = -1
                }
            }
        }

        data class Login(
            val id: String,
            val displayName: String?,
            val profilePictureUri: Uri?,
            val givenName: String?,
            val familyName: String?,
            val idToken: String,
            val phoneNumber: String?,
            val type: String,
            val data: Bundle,
        ) {
            companion object {
                fun from(cred: GoogleIdTokenCredential?): Login? {
                    return cred?.let {
                        Login(
                            id = it.id,
                            displayName = it.displayName,
                            profilePictureUri = it.profilePictureUri,
                            givenName = it.givenName,
                            familyName = it.familyName,
                            idToken = it.idToken,
                            phoneNumber = it.phoneNumber,
                            type = it.type,
                            data = it.data,
                        )
                    }
                }
            }
        }
    }
}
