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

dependencies {
    implementation(libs.androidx.compose.runtime)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    implementation("androidx.credentials:credentials:1.2.0-rc01")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.0-rc01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")

//    implementation("com.google.code.gson:gson:2.9.0")
//
//
//    implementation("com.google.api-client:google-api-client-android:2.2.0") {
//        exclude("org.apache.httpcomponents", "httpclient")
//        exclude("com.google.guava","guava-jdk5")
//    }
//
//    implementation("com.google.apis:google-api-services-youtube:v3-rev20231011-2.0.0" ) {
//        exclude("org.apache.httpcomponents")
//        exclude("com.google.guava","guava-jdk5")
//    }
//
//    implementation("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
//
//    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:chromecast-sender:0.28")

    testImplementation(projects.core.testing)
}