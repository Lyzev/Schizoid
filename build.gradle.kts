/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.fabric.loom)
}

idea {
	module {
		isDownloadSources = true
	}
}

base { archivesName.set(project.extra["archives_base_name"] as String) }

version = project.extra["mod_version"] as String
group = project.extra["maven_group"] as String

repositories {
    mavenCentral()
    maven("https://api.modrinth.com/maven")
    maven("https://jitpack.io")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(
        variantOf(libs.fabric.mappings) {
            classifier("v2")
        }
    )

    modImplementation(libs.bundles.fabric)

    // https://github.com/Lyzev
    implementation(libs.lyzev.events)
    implementation(libs.lyzev.settings)

    // https://github.com/ronmamo/reflections
    implementation(libs.reflections)

    // https://github.com/SpaiR/imgui-java
    implementation(libs.bundles.imgui)
}

loom {
    accessWidenerPath.set(File("src/main/resources/schizoid.accesswidener"))
}

tasks {

    val javaVersion = JavaVersion.toVersion((project.extra["java_version"] as String).toInt())

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    withType<KotlinCompile> { kotlinOptions { jvmTarget = javaVersion.toString() } }

    jar { from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } } }

    processResources {
        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "java" to project.extra["java_version"] as String,
                    "version" to project.extra["mod_version"] as String,
                    "minecraft" to libs.versions.minecraft.get(),
                    "fabricloader" to libs.versions.fabric.loader.get(),
                    "fabric_api" to libs.versions.fabric.api.get(),
                    "fabric_language_kotlin" to libs.versions.fabric.kl.get(),
                )
            )
        }
        filesMatching("*.mixins.json") { expand(mutableMapOf("java" to javaVersion.toString())) }
    }

    // Run `./gradlew wrapper --gradle-version <newVersion>` or `gradle wrapper --gradle-version <newVersion>` to update gradle scripts
	// BIN distribution should be sufficient for the majority of mods
	wrapper {
		distributionType = Wrapper.DistributionType.BIN
	}

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }

    dokkaHtml.configure {
		moduleName.set("Schizoid")
		dokkaSourceSets {
			configureEach {
				includes.from("dokka-docs.md")
				jdkVersion.set(javaVersion.toString().toInt())
			}
		}
	}
}

