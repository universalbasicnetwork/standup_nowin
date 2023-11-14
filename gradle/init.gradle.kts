/*
 * Copyright 2022 The Android Open Source Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *   Modified by Jay Bobzin for StandUp! sample application
 */

val ktlintVersion = "0.48.1"

initscript {
    val spotlessVersion = "6.13.0"

    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.diffplug.spotless:spotless-plugin-gradle:$spotlessVersion")
    }
}

rootProject {
    subprojects {

        if (project.path.startsWith(":standup") && (!project.subprojects.isEmpty() 
	        || project.path in setOf(":standup:integration:openid"))) {
	    // println("skipping project: ${project.path}")
            return@subprojects
	} 
        apply<com.diffplug.gradle.spotless.SpotlessPlugin>()
        // only check changes from _main
        extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            ratchetFrom("origin/su/_main")
            kotlin {
                target("**/*.kt")
                targetExclude("**/build/**/*.kt", "integration/openid/**/*.kt")
                ktlint(ktlintVersion).userData(mapOf("android" to "true"))
                licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
            }
            format("kts") {
                target("**/*.kts")
                targetExclude("**/build/**/*.kts", "integration/openid/**/*.kts")
                // Look for the first line that doesn't have a block comment (assumed to be the license)
                licenseHeaderFile(rootProject.file("spotless/copyright.kts"), "(^(?![\\/ ]\\*).*$)")
            }
            format("xml") {
                target("**/*.xml")
                targetExclude("**/build/**/*.xml", "**/openid/**/*.xml")
                // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
                licenseHeaderFile(rootProject.file("spotless/copyright.xml"), "(<[^!?])") 
            }
        }
    }
}
