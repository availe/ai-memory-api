import io.availe.buildlogic.getHostIpAddress

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.gradle.buildconfig)
}

val useLocalIp = providers.gradleProperty("availe.dev.useLocalIp").getOrElse("false").toBoolean()

group = "io.availe"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js {
        outputModuleName = "shared"
        browser()
        binaries.library()
        generateTypeScriptDefinitions()

        compilerOptions {
            target = "es2015"
        }
    }
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

    buildConfigField("String", "HOST_IP", hostIpProvider)
}