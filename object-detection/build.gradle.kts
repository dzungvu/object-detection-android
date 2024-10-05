plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.luke.object_detection"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    publishing {
        singleVariant("release")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.luke.object_detection"
                artifactId = "object-detection"
                version = "1.0.6"

                pom {
                    name.set("Object Detection")
                    description.set("Object Detection Library")
                    url.set("https://github.com/dzungvu/object-detection-android")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("dzungvu")
                            name.set("Dzung Vu")
                            email.set("dzungvucs@gmail.com") // Replace with your email
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/dzungvu/object-detection-android.git")
                        developerConnection.set("scm:git:ssh://github.com/dzungvu/object-detection-android.git")
                        url.set("https://github.com/dzungvu/object-detection-android")
                    }
                }

            }
        }

        repositories {
            maven {
                name = "SmartShopObjectDetection"
                url = uri("https://maven.pkg.github.com/dzungvu/object-detection-android")
                credentials {
                    username = System.getenv("GITHUB_USER") ?: ""
                    password = System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //region tensorflow lite
    api(libs.tensorflow.lite.gpu)
    api(libs.tensorflow.lite.support)
    api(libs.tensorflow.lite.metadata)
}