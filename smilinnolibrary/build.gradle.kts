import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    `maven-publish`
}
//val keystorePropertiesFile = rootProject.file("azure-configs.properties")
//val keystoreProperties = Properties().apply {
//    load(keystorePropertiesFile.inputStream())
//}


android {
    namespace = "com.smilinno.smilinnolibrary"
    compileSdk = 33

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            consumerProguardFiles("proguard-rules.pro", "consumer-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    kotlin {
        jvmToolchain(18)
    }
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(18)        // << --- ADD This
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //exo player
    api("com.google.android.exoplayer:exoplayer:2.18.3")
    //signalr
    api("com.microsoft.signalr:signalr:7.0.0")
    api("org.slf4j:slf4j-jdk14:1.7.25")
    //gson
    api("com.google.code.gson:gson:2.9.0")
    //retrofit
    api("com.squareup.retrofit2:retrofit:2.9.0")
    //gson converter
    api("com.squareup.retrofit2:converter-gson:2.7.1")

    api("com.squareup.okhttp3:okhttp:5.0.0-alpha.6")
    api("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.6")

}

afterEvaluate {
    publishing {
        publications {
            // Creates a Maven publication called "release".
            create<MavenPublication>("release") {
                // Applies the component for the release build variant.
                from(components["release"])

                // You can then customize attributes of the publication as shown below.
                groupId = "com.smilinno.smilinnolibrary" //<GROUP_ID>
                artifactId = "assistant-release" //<ARTIFACT_ID>
                version = "1.0.0" // Your package version
            }
        }
        // Repositories to which Gradle can publish artifacts
        repositories {
            maven {
                url = uri("https://azure.smilinno-dev.com/Smilinno/Assistant-Software/_packaging/assistant-sdk/maven/v1")
//                credentials {
//                    username = keystoreProperties["userName"] as? String
//                    password = keystoreProperties["azureMavenAccessToken"] as? String
//                }
            }
        }

    }
}