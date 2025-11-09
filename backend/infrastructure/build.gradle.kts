plugins {
    kotlin("jvm") version "2.3.0-Beta2"
}

group = "io.availe"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.backend.shared)
    implementation(platform(libs.docker.java.bom))
    implementation(libs.docker.java)
    implementation(libs.docker.java.transport.httpclient5)
    implementation(libs.keycloak.admin.client)
    implementation(libs.postgresql)
    implementation(libs.logback)
}

tasks.test {
    useJUnitPlatform()
}

val containersSetup by tasks.registering(JavaExec::class) {
    group = "infrastructure"
    description = "Ensures Podman/Docker containers are setup for the application."
    mainClass.set("io.availe.MainKt")
    classpath = sourceSets.main.get().runtimeClasspath
}

val destroyAll by tasks.registering(JavaExec::class) {
    group = "infrastructure"
    description = "Stops and removes all containers, volumes, and networks for the application."
    mainClass.set("io.availe.MainKt")
    classpath = sourceSets.main.get().runtimeClasspath
    args("--destroyAll")
}