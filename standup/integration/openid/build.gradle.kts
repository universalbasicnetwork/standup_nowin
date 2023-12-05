/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

plugins {
    alias(libs.plugins.nowinandroid.android.library)
    alias(libs.plugins.nowinandroid.android.hilt)
}

android {
    namespace = "net.openid.appauthdemo"
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }
}

dependencies {

    implementation(projects.standup.data.auth)

    api(libs.openid.appauth)
    implementation (libs.androidx.appcompat)
    implementation (libs.androidx.annotation)
    implementation (libs.openid.material)
    implementation (libs.glide)
    implementation (libs.okio)
    implementation (libs.joda.time)

    kapt (libs.glide.compiler)
}
