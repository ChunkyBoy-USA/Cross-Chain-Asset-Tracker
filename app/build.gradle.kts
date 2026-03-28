import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kapt)
}

val properties = Properties()
properties.load(project.rootProject.file("local.properties").inputStream())

android {
    namespace = "com.crosschain.assettracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.crosschain.assettracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        debug {
            isMinifyEnabled = false
            isDebuggable = true

            buildConfigField("String", "ETHEREUM_SEPOLIA_RPC_URL", properties.getProperty("ETHEREUM_SEPOLIA_RPC_URL"))
            buildConfigField("String", "ARBITRUM_SEPOLIA_RPC_URL", properties.getProperty("ARBITRUM_SEPOLIA_RPC_URL"))
            buildConfigField("String", "REOWN_PROJECT_ID", properties.getProperty("REOWN_PROJECT_ID"))
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "ETHEREUM_SEPOLIA_RPC_URL", properties.getProperty("ETHEREUM_SEPOLIA_RPC_URL"))
            buildConfigField("String", "ARBITRUM_SEPOLIA_RPC_URL", properties.getProperty("ARBITRUM_SEPOLIA_RPC_URL"))
            buildConfigField("String", "REOWN_PROJECT_ID", properties.getProperty("REOWN_PROJECT_ID"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes.add("META-INF/DISCLAIMER")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.security.crypto)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(platform(libs.reown.bom))
    // Core SDK
    implementation(libs.reown.core)
    // For dApp applications
    implementation(libs.reown.appkit)
    // For blockchain interaction
    implementation(libs.web3j.core)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
