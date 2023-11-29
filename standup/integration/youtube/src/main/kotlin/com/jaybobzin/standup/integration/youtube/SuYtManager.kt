/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.integration.youtube

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequest
import com.google.api.services.youtube.YouTubeRequestInitializer
import com.google.api.services.youtube.YouTubeScopes
import com.google.api.services.youtube.model.Playlist
import com.jaybobzin.standup.data.auth.AuthDataManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "SuYtManager"
class SuYtManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val config: Config,
    private val authDataManager: AuthDataManager
    ) {
    private val ioScope = CoroutineScope(Dispatchers.IO)

//    private val ytScopes = listOf(YouTubeScopes.YOUTUBE_READONLY)

    interface Config {
        val serverClientId: String
        val appName: String
        val apiKey: String
    }

    val googleLogin: MutableStateFlow<GoogleLogin?> = MutableStateFlow(null)
    val playlistList: MutableStateFlow<List<Playlist>?> = MutableStateFlow(null)

    private var credentialManager: CredentialManager = CredentialManager.create(applicationContext)

    private val success: MutableState<Int> = mutableIntStateOf(0)

    private val transport : HttpTransport = NetHttpTransport()
    private val jsonFactory: JsonFactory = GsonFactory()

    private val yt : SharedFlow<YouTube?> = authDataManager.tokensFlow.mapLatest { tokens ->
        return@mapLatest if (tokens == null) null else {
            val credential = GoogleCredential()
                .setAccessToken(tokens.accessToken)
//                .setExpirationTimeMilliseconds(tokens.accessTokenExpirationTime)
//                .setRefreshToken(tokens.refreshToken)
            val youtubeRequestInitializer = YouTubeRequestInitializer(config.apiKey)//CredentialedRequestInitializer(login)
            YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName(config.appName)
                .setYouTubeRequestInitializer(youtubeRequestInitializer)
                .build()
        }
    }.shareIn(ioScope, started = SharingStarted.Eagerly, replay = 1)


//    fun signInWithGoogle(
//        context: Context,
//    ) {
//        val googleSignInOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(config.serverClientId)
//            .build()
//
//        val request: GetCredentialRequest = GetCredentialRequest.Builder()
//            .addCredentialOption(googleSignInOption)
//            .build()
//
//        ioScope.launch {
//            try {
//                val result = credentialManager.getCredential(
//                    context = context,
//                    request = request,
//                )
//                handleCredentialResponse(result)
//            } catch (e: GetCredentialException) {
//                Timber.tag(TAG).e(e, "Failed to get credential")
//                success.value = -1
//            }
//        }
//    }
    fun getGoogleId(
        context: Context,
        autoLogin: Boolean = true
        ) {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(autoLogin)
            .setAutoSelectEnabled(autoLogin)
            .setServerClientId(config.serverClientId)
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
                handleCredentialResponse(result)
            } catch (e: NoCredentialException) {
                Timber.tag(TAG).i(e, "Not stored credential ($autoLogin)")
                if (autoLogin) {
                    //try again without autologin
                    getGoogleId(context, false)
                }

            } catch (e: GetCredentialException) {
                Timber.tag(TAG).e(e, "Failed to get credential")
                success.value = -1
            }
        }
    }

    fun handleCredentialResponse(result: GetCredentialResponse) {
        // Handle the successfully returned credential.

        when (val credential = result.credential) {
            is PublicKeyCredential -> {
                // Share responseJson such as a GetCredentialResponse on your server to
                // validate and authenticate
                val responseJson = credential.authenticationResponseJson
                Timber.tag(TAG).i("Got response json: %s", responseJson)
                success.value = 3
                googleLogin.value = null
            }

            is PasswordCredential -> {
                // Send ID and password to your server to validate and authenticate.
                val username = credential.id
                val password = credential.password
                Timber.tag(TAG).i("Got un $username and pw(${password.length}")
                success.value = 2
                googleLogin.value = null
            }

            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Timber.tag(TAG).i("Got googleIdToken $googleIdTokenCredential")
                        success.value = 1
                        googleLogin.value = GoogleLogin.from(googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        Timber.tag(TAG).e(e, "Received an invalid google id token response")
                        success.value = -1
                        googleLogin.value = null
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Timber.tag(TAG).e("Unexpected type of credential")
                    success.value = -1
                    googleLogin.value = null
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Timber.tag(TAG).e("Unexpected type of credential")
                success.value = -1
                googleLogin.value = null
            }
        }
    }

    fun fetchData() {
        ioScope.launch {
            yt.collectLatest {yt ->
                if (yt == null) {
                    Timber.tag(TAG).w("Youtube Client not available")
                    return@collectLatest
                }
                val parts = listOf<String>(
                    "contentDetails",
                    "id",
                    "localizations",
                    "player",
                    "snippet",
                    "status",
                )
                val list = try {
                    yt.playlists().list(parts)
                        .set("mine", true)
                        .execute()
                } catch (e: GoogleJsonResponseException) {
                    Timber.tag(TAG).w(e, "Request failed")
                    null
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "unexpected request failure")
                    null
                }
                if (!list.isNullOrEmpty()) {
                    Timber.tag(TAG).i("List returned: ${list.size}")
                    playlistList.value = list.items.toList()
                } else {
                    Timber.tag(TAG).d("List is empty")
                }
            }
        }

    }
}

//class CredentialedRequestInitializer(val login: GoogleLogin) : YouTubeRequestInitializer(null) {
//    override fun initializeYouTubeRequest(request: YouTubeRequest<*>?) {
//        request?.put("Authorization", "Bearer " + login.credential.idToken)
//    }
//}

data class GoogleLogin(
    val id: String,
    val displayName: String?,
    val profilePictureUri: Uri?,
    val givenName: String?,
    val familyName: String?,
    val idToken: String,
    val phoneNumber: String?,
    val type: String,
    val data: Bundle,
    val credential: GoogleIdTokenCredential,
) {
    companion object {
        fun from(cred: GoogleIdTokenCredential?): GoogleLogin? {
            return cred?.let {
                GoogleLogin(
                    id = it.id,
                    displayName = it.displayName,
                    profilePictureUri = it.profilePictureUri,
                    givenName = it.givenName,
                    familyName = it.familyName,
                    idToken = it.idToken,
                    phoneNumber = it.phoneNumber,
                    type = it.type,
                    data = it.data,
                    credential = it
                )
            }
        }
    }
}

