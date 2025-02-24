plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-kapt") // Habilita kapt para Room
}

android {
    namespace = "com.example.appdoctornow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appdoctornow"
        minSdk = 27
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // Room
    implementation ("androidx.room:room-runtime:2.6.1") // Runtime de Room
    implementation ("androidx.room:room-ktx:2.6.1")     // Soporte para corrutinas (KTX)
    kapt ("androidx.room:room-compiler:2.6.1")         // Compilador de Room (kapt para Kotlin)

    // Corrutinas
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // LiveData y ViewModel
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}