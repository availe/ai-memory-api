import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import io.availe.buildlogic.getHostIpAddress

plugins {
    kotlin("jvm") version "2.3.0-Beta2"
    alias(libs.plugins.gradle.buildconfig)
}

val useLocalIp = providers.gradleProperty("availe.dev.useLocalIp").getOrElse("false").toBoolean()

group = "io.availe"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

val hostIpProvider = providers.provider {
    if (useLocalIp) {
        "\"${getHostIpAddress()}\""
    } else {
        "\"localhost\""
    }
}

buildConfig {
    packageName("io.availe")
    className("NetworkConstants")

    useKotlinOutput {
        internalVisibility = false
    }

    sourceSets.named<BuildConfigSourceSet>("main") {
        buildConfigField("String", "HOST_IP", hostIpProvider)
    }
}