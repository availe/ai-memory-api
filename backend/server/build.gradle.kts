import org.flywaydb.gradle.task.AbstractFlywayTask

buildscript {
    dependencies {
        classpath(libs.flyway.database.postgresql)
    }
}

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.jooq.codegen)
    alias(libs.plugins.flyway)
    alias(libs.plugins.ktor)
}

application {
    mainClass.set("io.availe.MainKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

val dbUrl = providers.gradleProperty("aimemory.db.url")
val dbUser = providers.gradleProperty("aimemory.db.user")
val dbPassword = providers.gradleProperty("aimemory.db.password")

group = "io.availe"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.modelcontextprotocol.kotlin.sdk)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.xemantic.ai.tool.schema.mdc)

    implementation(libs.jooq)
    implementation(libs.hikariCP)
    jooqCodegen(libs.postgresql)
    implementation(libs.postgresql)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.pgvector)
}

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = dbUrl.get()
            user = dbUser.get()
            password = dbPassword.get()
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "ai_memory_api"
                excludes = "flyway_schema_history"
            }
            target {
                packageName = "io.availe.db.jooq"
            }
        }
    }
}

flyway {
    url = dbUrl.get()
    user = dbUser.get()
    password = dbPassword.get()
    schemas = arrayOf("ai_memory_api")
    createSchemas = true
}

tasks.compileKotlin {
    dependsOn(tasks.jooqCodegen)
}

tasks.jooqCodegen {
    dependsOn(tasks.flywayMigrate)
    inputs.files(fileTree("src/main/resources/db/migration"))
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.property("jdbc.url", dbUrl)
    inputs.property("jdbc.user", dbUser)
}

tasks.withType<AbstractFlywayTask> {
    notCompatibleWithConfigurationCache("because https://github.com/flyway/flyway/issues/3550")
}

tasks.test {
    useJUnitPlatform()
}