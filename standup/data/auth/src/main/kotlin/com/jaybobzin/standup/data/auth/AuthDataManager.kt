/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Singleton

private const val TAG = "AuthDataManager"

private const val AUTH_PREFS_FILENAME = "data.auth"
private const val AUTH_TOKENS_KEY = "tokens"

@Singleton
class AuthDataManager(@ApplicationContext context: Context) {
    private val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        AUTH_PREFS_FILENAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val json: Json = Json
    val tokensFlow = sharedPreferences.observeLatestObject(AUTH_TOKENS_KEY, AuthDataTokens.serializer(), json)
    fun tokensLoaded(tokens: AuthDataTokens) {
        sharedPreferences.storeObject(AUTH_TOKENS_KEY, tokens, AuthDataTokens.serializer(), json)
    }

}

@Serializable
data class AuthDataTokens (
    val idToken: String?,
    val refreshToken: String?,
    val accessToken: String?,
    val accessTokenExpirationTime: Long?,
) {
    companion object {
        fun from(
            idToken: String?,
            refreshToken: String?,
            accessToken: String?,
            accessTokenExpirationTime: Long?): AuthDataTokens {

            return AuthDataTokens(
                idToken,
                refreshToken,
                accessToken,
                accessTokenExpirationTime)

        }
    }
}


