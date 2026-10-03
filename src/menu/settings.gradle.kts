pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    // gradle/gradle-daemon-jvm.properties 가 정한 데몬 JDK 가 로컬에 없으면 받아올 수 있게 함
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    // 버전 카탈로그 gradle/libs.versions.toml 은 Gradle 기본 위치이므로 별도 선언 없이 libs 로 읽힘.
}

rootProject.name = "SEC-SeoulRND-menu"
include(":app")
