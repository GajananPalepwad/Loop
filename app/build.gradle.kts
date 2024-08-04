plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.gn4k.loop"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gn4k.loop"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/*.kotlin_module")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.firebase.inappmessaging.display)
    implementation(libs.firebase.messaging)
    implementation(libs.volley)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.vertexai)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.airbnb.android:lottie:6.3.0")
    implementation ("app.rive:rive-android:5.0.0")
    implementation ("androidx.startup:startup-runtime:1.1.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.github.OverflowArchives:AndroidLinkPreviewer:0.01")
    implementation ("com.github.yalantis:ucrop:2.2.7")

    implementation("com.github.MdFarhanRaja:SearchableSpinner:2.0")


    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.0")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

    implementation ("com.github.colourmoon:readmore-textview:v1.0.2")

    implementation ("live.videosdk:rtc-android-sdk:0.1.30")

    // library to perform Network call to generate a meeting id
    implementation ("com.amitshekhar.android:android-networking:1.0.2"){
        exclude(group = "com.android.support")
    }

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation ("com.github.MikeOrtiz:TouchImageView:1.4.1")

    implementation ("com.google.auth:google-auth-library-oauth2-http:1.19.0")




}
