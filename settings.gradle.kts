/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified by Jay Bobzin for StandUp! sample application.
 */

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "standup_nowin"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":standup:app")
include(":standup:core")
include(":standup:integration:youtube")
include(":standup:integration:openid")
include(":standup:data:auth")
include(":standup:common:compose")

include(":benchmarks")
include(":core:common")
include(":core:data")
include(":core:data-test")
include(":core:database")
include(":core:datastore")
include(":core:datastore-test")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:ui")
include(":core:testing")
include(":core:analytics")
include(":core:notifications")

include(":lint")
include(":sync:work")
include(":sync:sync-test")
include(":ui-test-hilt-manifest")
