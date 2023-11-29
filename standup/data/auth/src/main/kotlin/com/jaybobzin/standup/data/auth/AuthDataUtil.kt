/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.data.auth

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import timber.log.Timber

private const val TAG = "AuthDataUtil"
class AuthDataUtil
internal fun <T> SharedPreferences.observeKey(
    key: String,
    getter: (SharedPreferences, String) -> T?
): Flow<T?> = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, k ->
        if (key != k) {
            return@OnSharedPreferenceChangeListener
        }
        val value = getter(sp, key)
        if (isActive) {
            trySend(value)
        }
    }

    registerOnSharedPreferenceChangeListener(listener)

    //Sent existing value
    trySend(getter(this@observeKey, key))
    awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
}
internal fun SharedPreferences.observeString(key: String): Flow<String?> = observeKey(key) { sp, k ->
    try {
        sp.getString(k, null)
    } catch (e: ClassCastException) {
        Timber.tag(TAG).w("Not a string: $k")
        null
    }
}

internal fun <T> SharedPreferences.observeLatestObject(
    key: String,
    deserializer: DeserializationStrategy<T>,
    json: Json = Json
) : SharedFlow<T?> = observeString(key).mapLatest {
    it?.let { s ->
        json.decodeFromString(deserializer, s)
    }
}.shareInDefaults()

internal fun <T> SharedPreferences.storeObject(
    key: String,
    obj: T,
    serializer: KSerializer<T>,
    json: Json = Json
) {
    val tokensJson = json.encodeToString(serializer, obj)
    val editor = edit()
    editor.putString(key, tokensJson)
    editor.apply()
}

fun <T> Flow<T>.shareInDefaults(
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    started: SharingStarted = SharingStarted.WhileSubscribed(),
    replay: Int = 1
): SharedFlow<T> = this.shareIn( scope = scope, started = started, replay = replay)
fun <T> Flow<T?>.stateInDefaults(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(),
    initialValue: T? = null
): StateFlow<T?> = this.stateIn( scope = scope, started = started, initialValue = initialValue )
