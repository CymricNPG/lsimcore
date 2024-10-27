import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

plugins {
    kotlin("jvm") version libs.versions.kotlin
    id("org.jetbrains.dokka") version libs.versions.dokka
    id("org.sonarqube") version libs.versions.sonarqube
    id("com.github.ben-manes.versions") version libs.versions.versions
    application
}

group = "net.npg"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.bundles.junit)
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.commons.math3)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}

application {
    mainClass.set("MainKt")
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("documentation"))
}


tasks.withType<DokkaTask>().configureEach {
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = fileTree("src/main/kotlin") {
            include("**/*.png")
        }.toList()
    }
}
tasks.named("assemble") {
    dependsOn(tasks.dokkaHtml)
}

sonar {
    properties {
        property("sonar.host.url", "http://172.16.42.7:9000")
    }
}