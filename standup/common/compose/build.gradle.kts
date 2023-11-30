/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.jaybobzin.standup.common.compose"
}

dependencies {

    api(libs.androidx.compose.runtime)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.lifecycle.runtimeCompose)
    api(libs.androidx.hilt.navigation.compose)

    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.serialization.json)

    api(libs.timber)

    testImplementation(projects.core.testing)
}
