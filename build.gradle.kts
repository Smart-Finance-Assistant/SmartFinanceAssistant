// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.3" apply false // ✅ 명시적 버전 필요
    id("com.google.gms.google-services") version "4.4.2" apply false
}