plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
    id("kotlin-parcelize")
}

val appVersion = "0.1.2"

android {
    namespace = "com.kynarec.kmusic"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kynarec.kmusic"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // build with ./gradlew assembleRelease
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["appName"] = "KyNarec"
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures{
        compose = true
        buildConfig = true
        dataBinding {
            enable = true
        }
    }
    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "KMusic_v${appVersion}.apk"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)

    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.androidx.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.databinding.runtime)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.serialization.json)
    implementation(libs.androidx.recyclerview)
    implementation (libs.androidx.cardview)

    implementation (libs.glide)
    implementation (libs.newpipeextractor)
    implementation (libs.exoplayer)
    implementation (libs.okhttp)

    kapt(libs.compiler)

    implementation(libs.innertube)

    implementation( libs.androidx.room.runtime)
    debugImplementation(libs.androidx.compose.ui.tooling)
    kapt (libs.androidx.room.compiler)

    implementation( libs.toasty)

    implementation(libs.androidx.navigation.compose)
//    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.material)
    implementation(libs.coil.compose)
    // Add the Kotlinx Serialization library dependency here
    implementation(libs.kotlinx.serialization.json)

    // wavy seekbar
    implementation(libs.wavy.slider)

    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("org.json:json:20250517")

    implementation("eu.anifantakis:ksafe:1.1.1")
    implementation("eu.anifantakis:ksafe-compose:1.1.1")
    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.38.1")

    implementation("io.github.vinceglb:filekit-dialogs:0.12.0")
}