// NutriPlan – app modul build script
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.nutriplan.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nutriplan.app"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        // Stabil, tesztelési aláíró kulcs – minden build ugyanazzal a kulccsal íródik alá,
        // így az app helyben frissíthető marad. (Play Store-hoz külön, titkos kulcs kell.)
        create("release") {
            storeFile = file("signing/nutriplan-release.jks")
            storePassword = "nutriplan"
            keyAlias = "nutriplan"
            keyPassword = "nutriplan"
        }
    }

    buildTypes {
        release {
            // Kódzsugorítás (R8) és nem használt erőforrások eltávolítása
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // A release csomagot a stabil tesztkulccsal írjuk alá (közvetlenül telepíthető).
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
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
        compose = true
        // A BuildConfig kikapcsolva, mert nincs rá szükségünk (kevesebb metaadat)
        buildConfig = false
    }

    packaging {
        resources {
            // Felesleges és potenciálisan információt szivárogtató metaadat-fájlok kizárása
            // a végleges csomagból (méretcsökkentés + biztonság).
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                // Az AGP által injektált build-metaadat (AGP verzió) – több mintával is próbáljuk
                "META-INF/com/android/build/gradle/app-metadata.properties",
                "/META-INF/com/android/build/gradle/app-metadata.properties",
                "META-INF/com/android/build/**",
                "/META-INF/androidx/**/LICENSE.txt",
                "/META-INF/*.version",
                "/META-INF/*.kotlin_module",
                "/META-INF/CHANGES",
                "/META-INF/README.md",
                "DebugProbesKt.bin"
            )
        }
    }
}

dependencies {
    // AndroidX alaptechnológiák
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose (BOM-mal verziókezelve)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room adatbázis
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt függőséginjektálás
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Kotlinx – szerializáció és koroutinok
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore – beállítások tárolása
    implementation(libs.androidx.datastore.preferences)

    // Biometrikus zár
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)

    // Glance – kezdőképernyő-widget
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // ProfileInstaller – a könyvtárak baseline profiljainak telepítése (gyorsabb indulás)
    implementation(libs.androidx.profileinstaller)

    // Timber – naplózás
    implementation(libs.timber)

    // Tesztek
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Debug eszközök
    debugImplementation(libs.androidx.ui.tooling)
}
