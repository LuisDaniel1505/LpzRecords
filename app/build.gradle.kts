plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)


    alias(libs.plugins.kotlinx.serialization)
}


android {
    namespace = "com.ldaniel1505.lpzrecords"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.ldaniel1505.lpzrecords"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    //Implementaciones para la base de datos SUPABASE
    // Supabase y módulos necesarios
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.0")   // Maneja el Login/Registro
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.0") // Maneja las tablas (usuarios, productos)

    // Motor de Red para Android (Ktor)
    implementation("io.ktor:ktor-client-android:2.3.7")

    // Serialización (Para convertir JSON a objetos Kotlin fácilmente)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.patrykandpatrick.vico:compose:3.1.0")
    implementation("com.patrykandpatrick.vico:compose-m3:3.1.0")

    //implementation("androidx.compose.material:material-icons-extended:${compose_version}")
}