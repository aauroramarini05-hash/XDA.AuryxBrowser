plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

val geminiApiKey: String = (
    (project.findProperty("GEMINI_API_KEY") as String?)
        ?: System.getenv("GEMINI_API_KEY")
        ?: ""
).replace("\"", "\\\"")

android {
    namespace = "com.xdustatom.auryxbrowser"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xdustatom.auryxbrowser"
        minSdk = 23
        targetSdk = 34
        versionCode = 1407006
        versionName = "1.407.06"
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val signingStoreFilePathEnv = System.getenv("SIGNING_STORE_FILE")
    val signingStorePasswordEnv = System.getenv("SIGNING_STORE_PASSWORD")
    val signingKeyAliasEnv = System.getenv("SIGNING_KEY_ALIAS")
    val signingKeyPasswordEnv = System.getenv("SIGNING_KEY_PASSWORD")

    if (!signingStoreFilePathEnv.isNullOrBlank()
        && !signingStorePasswordEnv.isNullOrBlank()
        && !signingKeyAliasEnv.isNullOrBlank()
        && !signingKeyPasswordEnv.isNullOrBlank()
    ) {
        signingConfigs {
            create("release") {
                storeFile = file(signingStoreFilePathEnv)
                storePassword = signingStorePasswordEnv
                keyAlias = signingKeyAliasEnv
                keyPassword = signingKeyPasswordEnv
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false

            if (signingConfigs.findByName("release") != null) {
                signingConfig = signingConfigs.getByName("release")
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.coil-kt:coil:2.5.0")
    implementation("androidx.interpolator:interpolator:1.0.0")

    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.google.android.play:review-ktx:2.0.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
