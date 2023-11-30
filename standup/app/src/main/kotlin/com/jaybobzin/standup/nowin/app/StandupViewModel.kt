/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.nowin.app

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaybobzin.standup.data.auth.stateInDefaults
import com.jaybobzin.standup.integration.youtube.SuForegroundServiceBinder
import com.jaybobzin.standup.integration.youtube.SuYtManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import net.openid.appauthdemo.LoginActivity
import javax.inject.Inject

@HiltViewModel
class StandupViewModel @Inject constructor(
    @ApplicationContext applicationContext: Context,
    val ytManager: SuYtManager,
) : ViewModel() {

    private val mutableYtBinder: MutableStateFlow<SuForegroundServiceBinder?> = MutableStateFlow(null)
    val ytBinder = mutableYtBinder.asStateFlow()
    fun ytBound(binder: SuForegroundServiceBinder?) {
        mutableYtBinder.value = binder
    }

//    val mutableActivityFlow: MutableStateFlow<StandupActivity?> = MutableStateFlow(null)

//    val accountsFlow : StateFlow<List<Account>?> = mutableActivityFlow.map {
//        if (it == null) null else {
// //            val am = AccountManager.get(it)
// //            am.accounts.toList()
//            listOf<Account>()
//        }
//    }.stateInDefaults( scope = viewModelScope )

    val countdownFlow: StateFlow<Int?> = ytBinder.flatMapLatest {
        it?.countdownFlow ?: flowOf(null)
    }.onEach {
        ytBinder.value?.countdown(it ?: -1)
    }.stateInDefaults(scope = viewModelScope)
    fun loginGoogle(activity: StandupActivity) {
        Intent(activity, LoginActivity::class.java).also { intent -> activity.startActivity(intent) }
    }
}
