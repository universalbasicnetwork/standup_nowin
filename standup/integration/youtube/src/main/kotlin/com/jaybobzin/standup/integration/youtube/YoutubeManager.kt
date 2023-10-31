/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */
package com.jaybobzin.standup.integration.youtube

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

object Youtube {
    class Manager @Inject constructor(@ApplicationContext context: Context) {

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
        val data: Bundle
    ) {
        companion object {
            fun from(cred : GoogleIdTokenCredential?) : Login? {
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
                        data = it.data
                    )
                }
            }
        }

    }
}