plugins {
    alias(libs.plugins.kotlinJvm)
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
    implementation("org.http4k:http4k-api-ui-redoc")
}

tasks.test {
    useJUnitPlatform()
}