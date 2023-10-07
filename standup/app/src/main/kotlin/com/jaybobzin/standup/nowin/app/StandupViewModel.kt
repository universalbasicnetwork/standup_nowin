/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

package com.jaybobzin.standup.nowin.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StandupViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context,
) : ViewModel() {
    init {
        viewModelScope.launch {

        }
    }

    val countdownFlow: StateFlow<Int?> = flow {
        for (i in 3 downTo 0) {
            emit(i)
            delay(2000)  // delay for 2 seconds
        }
    }.stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
    )
}
