/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.library.compose)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "com.jaybobzin.standup.core"
}

dependencies {
    implementation(projects.standup.common.compose)
}