plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.availe"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.http4k.bom))
    implementation(libs.http4k.core)
    implementation(libs.http4k.server.helidon)
    implementation(libs.http4k.api.openapi)
    implementation(libs.http4k.format.jackson)
    implementation(libs.http4k.multipart)
    implementation(libs.http4k.client.okhttp)

    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    implementation(libs.modelcontextprotocol.kotlin.sdk)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.xemantic.ai.tool.schema.mdc)
}

tasks.test {
    useJUnitPlatform()
}