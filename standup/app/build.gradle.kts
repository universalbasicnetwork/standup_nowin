/* Copyright 2023 Jay Bobzin SPDX-License-Identifier: Apache-2.0 */

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.nowinandroid.android.application)
    alias(libs.plugins.nowinandroid.android.application.compose)
    alias(libs.plugins.nowinandroid.android.application.flavors)
    alias(libs.plugins.nowinandroid.android.hilt)
//    alias(libs.plugins.nowinandroid.android.room)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.jaybobzin.standup.nowin.app"

    defaultConfig {
        applicationId = "com.jaybobzin.standup.nowin.app"
        versionCode = 1
        versionName = "0.0.1"

        manifestPlaceholders[ "appAuthRedirectScheme"] = "com.googleusercontent.apps.748057129388-hu4bn24jdgggdubqpf2959f8lceeei8n"

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
            excludes.add("META-INF/DEPENDENCIES")
        }
    }
}

secrets {
    defaultPropertiesFileName = "secrets.defaults.properties"
}

dependencies {
    // TODO
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
