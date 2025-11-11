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

val dbUrl = providers.gradleProperty("aimemory.db.url")
val dbUser = providers.gradleProperty("aimemory.db.user")
val dbPassword = providers.gradleProperty("aimemory.db.password")
val keycloakUser = providers.gradleProperty("keycloak.admin.user")
val keycloakPassword = providers.gradleProperty("keycloak.admin.password")

fun JavaExec.setAppSystemProperties() {
    systemProperty("aimemory.db.url", dbUrl)
    systemProperty("aimemory.db.user", dbUser)
    systemProperty("aimemory.db.password", dbPassword)
    systemProperty("keycloak.admin.user", keycloakUser)
    systemProperty("keycloak.admin.password", keycloakPassword)
}

val containersSetup by tasks.registering(JavaExec::class) {
    group = "infrastructure"
    description = "Ensures Podman/Docker containers are setup for the application."
    mainClass.set("io.availe.MainKt")
    classpath = sourceSets.main.get().runtimeClasspath
    setAppSystemProperties()
}

val destroyAll by tasks.registering(JavaExec::class) {
    group = "infrastructure"
    description = "Stops and removes all containers, volumes, and networks for the application."
    mainClass.set("io.availe.MainKt")
    classpath = sourceSets.main.get().runtimeClasspath
    args("--destroyAll")
    setAppSystemProperties()
}