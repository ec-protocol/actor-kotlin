version = "0.0.1"

plugins {
    idea
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
}

kotlin {
    sourceSets.forEach {
        when (it.name) {
            "main" -> {
                it.kotlin.srcDirs("src")
            }
            "test" -> {
                it.kotlin.srcDirs("test")
            }
        }
    }
}

tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("io.ktor:ktor-network:1.4.0")
    implementation("com.github.ajalt.clikt:clikt:3.0.1")
}
