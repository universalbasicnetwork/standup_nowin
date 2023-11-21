/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.jaybobzin.standup.data.auth"
}

dependencies {

    implementation("androidx.security:security-crypto:1.0.0")

    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.timber)

    testImplementation(projects.core.testing)
}
