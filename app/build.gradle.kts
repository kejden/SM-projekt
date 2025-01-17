plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.pb.wi.projekt"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.pb.wi.projekt"
        minSdk = 24
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
}

dependencies {
    implementation("com.github.yuyakaido:CardStackView:v2.3.4")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation("com.github.bumptech.glide:glide:4.13.2")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}