/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.nowinandroid.android.application)
    alias(libs.plugins.nowinandroid.android.application.compose)
    alias(libs.plugins.nowinandroid.android.hilt)
    alias(libs.plugins.nowinandroid.android.room)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.jaybobzin.standup.nowin.app"

    defaultConfig {
        applicationId = "com.jaybobzin.standup.nowin.app"
        versionCode = 1
        versionName = "0.0.1"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
    }

//    productFlavors {
//        dev {
//            resourceConfigurations "en", "xxhdpi"
//        }
//    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.twitter) {
        exclude(group = "org.apache.oltu.oauth2")
    }
}
