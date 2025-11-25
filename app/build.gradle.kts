plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.apollo)
    alias(libs.plugins.compose.compiler)

    id("io.sentry.android.gradle") version "5.7.0"
}

android {
    namespace = "io.mkg20001.nixosimage"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.mkg20001.nixosimage"
        minSdk = 35
        targetSdk = 36
        versionCode = 8
        versionName = "0.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "ALLOW_ANY_METHOD", "true")
        }
        release {
            buildConfigField("boolean", "ALLOW_ANY_METHOD", "false")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.apollo.runtime)
    implementation(libs.libsu.core)

    // https://mvnrepository.com/artifact/org.apache.commons/commons-compress
    implementation("org.apache.commons:commons-compress:1.27.1")

    // for permission granting
    // implementation("androidx.test:rules:1.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // graphql
    /* implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.5")
    implementation("com.apollographql.apollo:apollo-api:4.1.1") */

    val nav_version = "2.8.9"

    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Optional - Included automatically by material, only add when you need
    // the icons but not the material library (e.g. when using Material3 or a
    // custom design system based on Foundation)
    implementation(libs.androidx.compose.material.icons.core)
    // Optional - Add full set of material icons
    implementation(libs.androidx.compose.material.icons.extended)
    // Optional - Add window size utils
    // implementation("androidx.compose.material3.adaptive:adaptive")

    // Optional - Integration with ViewModels
    // implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    // Optional - Integration with LiveData
    // implementation("androidx.compose.runtime:runtime-livedata")

    // implementation("androidx.lifecycle:lifecycle-viewmodel-ktx")

    // https://mvnrepository.com/artifact/tools.fastlane/screengrab
    implementation("tools.fastlane:screengrab:2.1.1")

    implementation("com.github.jeziellago:compose-markdown:0.5.7")

    // implementation("com.yazantarifi:markdown-compose:1.0.4")
}

apollo {
    service("service") {
        packageName.set("io.mkg20001.nixosimage")
        introspection {
            endpointUrl.set("https://api.github.com/graphql")
            schemaFile.set(file("src/main/graphql/schema.graphqls"))
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.test.espresso:espresso-core:3.5.0")
    }
}

sentry {
    org.set("nixos-avf")
    projectName.set("nixos-avf-image-app")

    // this will upload your source code to Sentry to show it as part of the stack traces
    // disable if you don't want to expose your sources
    includeSourceContext.set(true)
}
