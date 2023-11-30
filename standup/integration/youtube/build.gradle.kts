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
    api(libs.google.id)

    api("net.openid:appauth:0.11.1")
    api ("pub.devrel:easypermissions:3.0.0")
    api("com.google.api-client:google-api-client-android:1.33.0")
    api("com.google.apis:google-api-services-youtube:v3-rev20231011-2.0.0")
    api(group = "com.google.http-client", name= "google-http-client", version= "1.22.0")
    implementation("com.google.code.gson:gson:2.10.1")


    testImplementation(projects.core.testing)
}