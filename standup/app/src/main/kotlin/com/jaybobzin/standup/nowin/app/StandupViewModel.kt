/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.nowin.app

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaybobzin.standup.integration.youtube.YoutubeService.YtBinder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StandupViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context,
) : ViewModel() {

    internal val success: MutableState<Int> = mutableIntStateOf(0)

    private val mutableYtBinder: MutableStateFlow<YtBinder?> = MutableStateFlow(null)
    val ytBinder = mutableYtBinder.asStateFlow()
    fun ytBound(binder: YtBinder?) {
        mutableYtBinder.value = binder
    }

    val countdownFlow: StateFlow<Int?> = ytBinder.flatMapLatest {
        it?.countdownFlow ?: flowOf(null)
    }.stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
    )
}
