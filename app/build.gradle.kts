plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.secretGradlePlugin)
}

android {
    namespace = "com.luke.objectdetection"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.luke.objectdetection"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        mlModelBinding = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    //region media3
//    implementation(libs.androidx.media3.exoplayer)
//    implementation(libs.androidx.media3.ui)
//    //support hls
//    // Other dependencies
//    implementation(libs.androidx.media3.exoplayer.hls)

//    implementation(project(":object-detection"))
    implementation(libs.objectt.detection)

    //region exoplayer 2
    implementation(libs.exoplayer)
    implementation(libs.exoplayer.hls)
    implementation(libs.exoplayer.ui)


    //region camera
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    //google gemini
//    implementation(libs.google.api.client)
//    implementation(libs.google.http.client.gson)

    implementation(libs.generativeai)

    //network
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    //log network
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //image
    implementation(libs.glide)


}