plugins {

    /**
     * Use `apply false` in the top-level build.gradle file to add a Gradle
     * plugin as a build dependency but not apply it to the current (root)
     * project. Don't use `apply false` in sub-projects. For more information,
     * see Applying external plugins with same version to subprojects.
     */
//    id("com.android.application") version "8.10.1" apply false
//    id("com.android.library") version "8.10.1" apply false
//    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
//    id("com.google.gms.google-services") version "4.3.10" apply false

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.gms.google.services) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
}
