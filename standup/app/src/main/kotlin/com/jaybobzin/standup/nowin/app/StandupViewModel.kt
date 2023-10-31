/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.nowin.app

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.jaybobzin.standup.integration.youtube.SuForegroundServiceBinder
import com.jaybobzin.standup.integration.youtube.Youtube
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StandupViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context,
) : ViewModel() {

    val googleIdToken: MutableState<Youtube.Login?> = mutableStateOf(null)
    internal val success: MutableState<Int> = mutableIntStateOf(0)

    private val mutableYtBinder: MutableStateFlow<SuForegroundServiceBinder?> = MutableStateFlow(null)
    val ytBinder = mutableYtBinder.asStateFlow()
    fun ytBound(binder: SuForegroundServiceBinder?) {
        mutableYtBinder.value = binder
    }

    val countdownFlow: StateFlow<Int?> = ytBinder.flatMapLatest {
        it?.countdownFlow ?: flowOf(null)
    }.onEach {
        ytBinder.value?.countdown(it ?: -1)
    }.stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
    )
}
