/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.jaybobzin.standup.integration.youtube"
    buildFeatures {
        viewBinding = true
    }
    unitTestVariants.all {
        this.mergedFlavor.manifestPlaceholders["appAuthRedirectScheme"] = "com.jaybobzin.standup.nowin.app"
    }
}

configurations {
    all {
        exclude( group= "org.apache.httpcomponents", module= "httpclient")
    }
}


dependencies {
    implementation(projects.standup.data.auth)
    implementation(projects.standup.common.compose)

    api(libs.androidx.credentials.credentials)
    api(libs.androidx.credentials.play.services.auth)
    api(libs.appauth)
    api(libs.easypermissions)
    api(libs.google.api.client.android)
    api(libs.google.api.services.youtube)
    api(libs.google.http.client)
    api(libs.google.id)

    implementation(libs.gson)

    testImplementation(projects.core.testing)
}