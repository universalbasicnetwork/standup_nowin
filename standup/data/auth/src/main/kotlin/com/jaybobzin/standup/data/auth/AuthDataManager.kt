/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthDataManager"

private const val AUTH_PREFS_FILENAME = "data.auth"
private const val AUTH_TOKENS_KEY = "tokens"

@Singleton
class AuthDataManager @Inject constructor(@ApplicationContext context: Context) {
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
    // Replace for testing
    internal var currentTimeFn: () -> Long = System::currentTimeMillis
    fun isExpired(): Boolean {
        return accessTokenExpirationTime != null && accessTokenExpirationTime <= currentTimeFn()
    }

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


