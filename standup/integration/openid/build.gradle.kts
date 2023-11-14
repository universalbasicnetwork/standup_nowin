/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

plugins {
    alias(libs.plugins.nowinandroid.android.library)
}

android {
    namespace = "net.openid.appauthdemo"
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }
}

dependencies {
    api("net.openid:appauth:0.11.1")
    implementation ("androidx.appcompat:appcompat:1.3.0")
    implementation ("androidx.annotation:annotation:1.2.0")
    implementation ("com.google.android.material:material:1.3.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.squareup.okio:okio:2.10.0")
    implementation ("joda-time:joda-time:2.10.10")

    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
}