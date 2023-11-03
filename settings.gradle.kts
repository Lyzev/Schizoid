/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
    }
}

buildscript {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.github.Lyzev:Network4K:1.3")
    }
}

rootProject.name = "Schizoid"
