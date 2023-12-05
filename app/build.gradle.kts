import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.smilinno.projectlibrary"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.smilinno.projectlibrary"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    kotlin {
        jvmToolchain(18)
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(project(mapOf("path" to ":smilinnolibrary")))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
    //exo player
    implementation ("com.google.android.exoplayer:exoplayer:2.18.3")
    // delete
    implementation ("com.github.skydoves:balloon:1.6.0")
    //signalr
    implementation ("com.microsoft.signalr:signalr:7.0.0")
    implementation ("org.slf4j:slf4j-jdk14:1.7.25")
    //gson
    implementation("com.google.code.gson:gson:2.9.0")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    //gson converter
    implementation("com.squareup.retrofit2:converter-gson:2.7.1")
    //lottie
    implementation("com.airbnb.android:lottie:4.1.0")

    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.6")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.6")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

kapt {
    correctErrorTypes = true
}



