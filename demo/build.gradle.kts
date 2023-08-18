/*
 * Copyright (C) 2015-2023 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    id("com.android.application")
    kotlin("android")
    id("com.github.triplet.play")
}

fun Project.get(name: String, def: String = "$name not found") =
    properties[name]?.toString() ?: System.getenv(name) ?: def

android {
    namespace = "com.onegravity.contactpicker.demo"

    compileSdk = Build.compileSdkVersion
    buildToolsVersion = Build.buildToolsVersion

    defaultConfig {
        applicationId = "com.onegravity.contactpicker.demo"
        minSdk = Build.minSdkVersion
        targetSdk = Build.targetSdkVersion

        versionCode = project.properties["BUILD_NUMBER"]
            ?.toString()?.toInt()?.minus(1643908089)
            ?: 124

        versionName = Build.versionName
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.get("ONEGRAVITY_KEYSTORE_FILE"))
            storePassword = project.get("ONEGRAVITY_KEYSTORE_PASSWORD")
            keyAlias = project.get("ONEGRAVITY_OPENSOURCE_KEY_ALIAS")
            keyPassword = project.get("ONEGRAVITY_OPENSOURCE_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName(name)
        }

        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName(name)
        }
    }
}

dependencies {
    // use this to compile/run locally
    implementation(project(":library"))
    // or this to use the latest published library
//    implementation("com.1gravity:android-contactpicker:_")
}

play {
    val apiKeyFile = project.get("googlePlayApiKey")
    serviceAccountCredentials.set(file(apiKeyFile))
    track.set("internal")
}
