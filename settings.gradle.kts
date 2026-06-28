pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.google.gms.google-services" ->
                    useModule("com.google.gms:google-services:${requested.version}")
                "com.google.firebase.crashlytics" ->
                    useModule("com.google.firebase:firebase-crashlytics-gradle:${requested.version}")
                "com.google.firebase.firebase-perf" ->
                    useModule("com.google.firebase:perf-plugin:${requested.version}")
            }
        }
    }
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Save"
include(":app")
