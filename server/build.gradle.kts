plugins {
    kotlin("jvm") version "2.3.0-Beta2"
}

group = "io.availe"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}