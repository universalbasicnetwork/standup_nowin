/*
 * Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0
 */

package com.jaybobzin.standup.integration.youtube

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

import java.security.GeneralSecurityException

class YoutubeManager {
//    private val CLIENT_SECRETS = "client_secret.json"
    private val CLIENT_SECRETS = GoogleClientSecrets()
    private val SCOPES: Collection<String> =
        mutableListOf("https://www.googleapis.com/auth/youtube.readonly")

    private val APPLICATION_NAME = "API code samples"
    private val JSON_FACTORY: Gson = Gson()

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun authorize(httpTransport: NetHttpTransport?): Credential {
        // Build flow and trigger user authorization request.
        val flow: GoogleAuthorizationCodeFlow =
            Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .build()
        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun getService(): YouTube? {
        val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credential: Credential = authorize(httpTransport)
        return Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
}