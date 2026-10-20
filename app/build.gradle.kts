import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Chave do Google Maps (nunca versionada). Ordem: -PMAPS_API_KEY, variável de
// ambiente MAPS_API_KEY (GitHub Actions) ou MAPS_API_KEY=... no local.properties.
// Sem chave, o app mostra o mapa esquemático das paradas no lugar do Google Maps.
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
val mapsApiKey: String = providers.gradleProperty("MAPS_API_KEY").orNull
    ?: System.getenv("MAPS_API_KEY")
    ?: localProperties.getProperty("MAPS_API_KEY")
    ?: ""

android {
    namespace = "br.com.rotacerta.colaborador"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.rotacerta.colaborador"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        // 10.0.2.2 = localhost da máquina host visto pelo emulador Android.
        // Em aparelho físico, altere pelo ícone de engrenagem na tela de login
        // ou gere o APK com -PapiBaseUrl=http://IP-DO-PC:8000/api/
        val apiBaseUrl = providers.gradleProperty("apiBaseUrl").getOrElse("http://10.0.2.2:8000/api/")
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("boolean", "MAPS_ENABLED", "${mapsApiKey.isNotBlank()}")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.maps.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
