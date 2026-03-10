plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "unc.edu.pe.transportcax"
    compileSdk = 36

    defaultConfig {
        applicationId = "unc.edu.pe.transportcax"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "1.5"

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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit y Gson (Para Conectar a la API de Render)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Google Maps y Servicios de Ubicación (Para Geolocalización)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.maps.android:android-maps-utils:3.8.0")

    // ViewModel y LiveData (Para Arquitectura MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")

    // Diseño de Interfaz (UI)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.11.0")

    // --- FIREBASE Y GOOGLE LOG-IN ---
    // El BOM controla las versiones automáticamente
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))

    // 1. Base de datos en tiempo real (Para guardar y leer las alertas)
    implementation("com.google.firebase:firebase-firestore")

    // 2. Seguridad de Firebase (Para validar quién es el usuario)
    implementation("com.google.firebase:firebase-auth")

    // 3. Ventana emergente oficial de Google (Para elegir la cuenta de Gmail)
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // API Fused Location Provider
    implementation("com.google.android.gms:play-services-location:21.0.1")

}