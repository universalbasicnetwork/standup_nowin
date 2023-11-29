/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.data.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val TAG = "AuthDataViewModel"
@HiltViewModel
class AuthDataViewModel @Inject constructor(val authDataManager: AuthDataManager) : ViewModel() {
    val status : MutableLiveData<Int> = MutableLiveData(0)

    val tokensFlow = authDataManager.tokensFlow.stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed()
    )

    val tokensLd = authDataManager.tokensFlow.asLiveData(viewModelScope.coroutineContext)
}
