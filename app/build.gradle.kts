plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // python support
    id("com.chaquo.python")
    kotlin("kapt")
}

android {
    namespace = "com.kynarec.kmusic"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kynarec.kmusic"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // python support
        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        dataBinding {
            enable = true
        }
    }
}

chaquopy {
    defaultConfig {
        pip {
            install("innertube==2.1.19")  // Use == for exact version

        }
    }
    productFlavors { }
    sourceSets { }
}

dependencies {

    implementation(project(":innertube"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.databinding.runtime)
    implementation(libs.ktor.client.core)
    implementation(libs.androidx.recyclerview)
    implementation (libs.androidx.cardview)

    implementation (libs.glide)
    implementation (libs.newpipeextractor)
    implementation (libs.exoplayer)
    implementation (libs.okhttp)

    annotationProcessor (libs.compiler)

    implementation(libs.innertube)

    implementation( libs.androidx.room.runtime)
    kapt (libs.androidx.room.compiler)

    implementation( libs.toasty)
}