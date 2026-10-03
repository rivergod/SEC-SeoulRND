import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

// 빌드 설정 일부를 저장소에 커밋되지 않는 local.properties 에서 읽음.
// 우선순위: 명령줄 `-P<이름>=…` > local.properties > 기본값.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun buildSetting(name: String): String? = providers.gradleProperty(name).orNull ?: localProps.getProperty(name)

// 식단 데이터 출처 — remote(웰스토리 메뉴 API, 기본값) | sample(네트워크 없이 쓰는 예시 데이터).
val menuSource: String = buildSetting("seoulrnd.menuSource") ?: "remote"
require(menuSource in setOf("sample", "remote")) { "seoulrnd.menuSource 는 sample 또는 remote 여야 함: $menuSource" }

// release 서명 값은 local.properties(또는 -P)의 signing.* 에서 읽음. 소스에 경로·암호를 적지 않음.
// 설정이 없으면 release 는 서명되지 않은 채로 빌드됨.
val releaseStoreFile: String? = buildSetting("signing.storeFile")

android {
    namespace = "net.rivergod.sec.seoulrnd.android.menu"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "net.rivergod.sec.seoulrnd.android.menu"
        // 0.9.14 와 같은 최소 사양을 유지함. java.time 은 desugaring 으로 제공 (아래 compileOptions)
        minSdk = 24
        targetSdk = 35
        versionCode = 916
        versionName = "0.9.16"

        buildConfigField("boolean", "MENU_REMOTE", "${menuSource == "remote"}")
    }

    signingConfigs {
        // 저장소에 포함된 공용 디버그 키 — 개발 PC 가 달라도 debug 빌드를 덮어 설치할 수 있게 함
        getByName("debug") {
            storeFile = rootProject.file("keystores/debug.keystore")
        }
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = buildSetting("signing.storePassword")
                keyAlias = buildSetting("signing.keyAlias")
                keyPassword = buildSetting("signing.keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (releaseStoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
    }

    testOptions {
        // JVM 단위 테스트에서 android.util.Log 등 android.jar stub 호출이 예외 대신 기본값을 돌려주게 함
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // 웰스토리 메뉴 API 호출 (seoulrnd.menuSource=remote)
    implementation(libs.okhttp)
    implementation(libs.firebase.analytics)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)
    testImplementation(libs.org.json)
}
