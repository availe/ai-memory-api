package io.availe

import java.io.File
import java.util.*

fun loadGradleProperties(): Properties {
    val mergedProps = Properties()

    val gradlePropertiesFilePath = File(System.getProperty("user.home"), ".gradle/gradle.properties")

    if (gradlePropertiesFilePath.exists()) {
        try {
            gradlePropertiesFilePath.reader().use { mergedProps.load(it) }
        } catch (e: Exception) {
            println("Warning: Could not load fallback ~/.gradle/gradle.properties: ${e.message}")
        }
    }

    mergedProps.putAll(System.getProperties())

    return mergedProps
}