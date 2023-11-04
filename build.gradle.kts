/*
 * Copyright (c) 2023. Schizoid
 * All rights reserved.
 */

import com.google.gson.JsonParser
import groovy.util.Node
import groovy.xml.XmlParser
import me.lyzev.network.http.HttpClient
import me.lyzev.network.http.HttpMethod
import org.gradle.internal.classpath.Instrumented
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

version = if (System.getenv("CI") != null) "nightly-build" else project.extra["mod_version"] as String
group = project.extra["maven_group"] as String

repositories {
    mavenCentral()
    maven("https://api.modrinth.com/maven")
    maven("https://jitpack.io")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(
        variantOf(libs.yarn.mappings) {
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
    accessWidenerPath.set(File("src/main/resources/${project.extra["archives_base_name"] as String}.accesswidener"))
}

tasks.register("updateFabric") {
    group = project.extra["archives_base_name"] as String
    description = "Update Fabric Library Versions"
    doLast {
        val gameVersion = HttpClient.request(HttpMethod.GET, "https://meta.fabricmc.net/v2/versions/game").let { data ->
            JsonParser.parseString(data.toString()).asJsonArray.first { version -> version.asJsonObject["stable"].asBoolean }!!.asJsonObject["version"].asString
        }

        val mappingsVersion =
            HttpClient.request(HttpMethod.GET, "https://meta.fabricmc.net/v2/versions/yarn").let { data ->
                JsonParser.parseString(data.toString()).asJsonArray.first { version -> version.asJsonObject["gameVersion"].asString == gameVersion }!!.asJsonObject["version"].asString
            }

        val loaderVersion =
            HttpClient.request(HttpMethod.GET, "https://meta.fabricmc.net/v2/versions/loader").let { data ->
                JsonParser.parseString(data.toString()).asJsonArray.first { version -> version.asJsonObject["stable"].asBoolean }!!.asJsonObject["version"].asString
            }

        val apiVersion = HttpClient.request(
            HttpMethod.GET, "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml"
        ).let { data ->
            ((XmlParser().parseText(data.toString()).children()
                .first { node -> (node as Node).name() == "versioning" } as Node).children()
                .first { node -> (node as Node).name() == "versions" } as Node).children()
                .map { node -> (node as Node).text() }.last { version -> version.endsWith(gameVersion) }
        }

        val klVersion = HttpClient.request(
            HttpMethod.GET, "https://maven.fabricmc.net/net/fabricmc/fabric-language-kotlin/maven-metadata.xml"
        ).let { data ->
            ((XmlParser().parseText(data.toString()).children()
                .first { node -> (node as Node).name() == "versioning" } as Node).children()
                .first { node -> (node as Node).name() == "latest" } as Node).text()
        }

        val loomVersion = HttpClient.request(
            HttpMethod.GET, "https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml"
        ).let { data ->
            ((XmlParser().parseText(data.toString()).children()
                .first { node -> (node as Node).name() == "versioning" } as Node).children()
                .first { node -> (node as Node).name() == "latest" } as Node).text()
        }

        val versions = mapOf(
            "minecraft" to gameVersion,
            "yarn_mappings" to mappingsVersion,
            "fabric_loader" to loaderVersion,
            "fabric_api" to apiVersion,
            "fabric_kl" to klVersion,
            "fabric_loom" to loomVersion,
        ).also { println("-------LATEST-VERSIONS-------\n$it") }

        val tomlFile = file("gradle/libs.versions.toml")
        val tomlContent = tomlFile.readText()
        val updatedTomlContent =
            updateTomlVersions(tomlContent, versions).also { println("-------UPDATED-LIBS-------\n$it") }
        tomlFile.writeText(updatedTomlContent)
    }
}

tasks.register("updateKotlin") {
    group = project.extra["archives_base_name"] as String
    description = "Update Kotlin and Dokka Versions"
    doLast {
        val kotlinVersion = HttpClient.request(
            HttpMethod.GET, "https://api.github.com/repos/JetBrains/kotlin/releases/latest"
        ).let { data ->
            JsonParser.parseString(data.toString()).asJsonObject["target_commitish"].asString
        }

        val dokkaVersion = HttpClient.request(
            HttpMethod.GET, "https://api.github.com/repos/Kotlin/dokka/releases/latest"
        ).let { data ->
            JsonParser.parseString(data.toString()).asJsonObject["target_commitish"].asString
        }

        val versions = mapOf(
            "kotlin" to kotlinVersion,
            "dokka" to dokkaVersion,
        ).also { println("-------LATEST-VERSIONS-------\n$it") }

        val tomlFile = file("gradle/libs.versions.toml")
        val tomlContent = tomlFile.readText()
        val updatedTomlContent =
            updateTomlVersions(tomlContent, versions).also { println("-------UPDATED-LIBS-------\n$it") }
        tomlFile.writeText(updatedTomlContent)
    }
}

fun updateTomlVersions(tomlContent: String, versions: Map<String, String>): String {
    val lines = tomlContent.lines()
    val updatedLines = lines.map { line ->
        versions.entries.fold(line) { acc, (key, value) ->
            acc.replace("$key = \"[^\"]+\"".toRegex(), "$key = \"$value\"")
        }
    }
    return updatedLines.joinToString("\n")
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
                    "version" to if (System.getenv("CI") != null) "nightly-build" else project.extra["mod_version"] as String,
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
		moduleName.set(project.extra["archives_base_name"] as String)
		dokkaSourceSets {
			configureEach {
				includes.from("dokka-docs.md")
				jdkVersion.set(javaVersion.toString().toInt())
			}
		}
	}
}

