/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.jaybobzin.standup.core"
}

dependencies {
    implementation(projects.standup.integration.youtube)
    implementation(projects.standup.integration.openid)
    implementation(projects.standup.data.auth)
    implementation(projects.standup.common.compose)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.guava)
}