plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "kr.napjaki.wallpaper"
    compileSdk = 35
    defaultConfig {
        applicationId = "kr.napjaki.wallpaper"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
