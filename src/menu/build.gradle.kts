// Top-level build file. 플러그인 버전은 gradle/libs.versions.toml 에서만 정하고 모듈에서 적용함.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
}
