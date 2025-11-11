package io.availe.orchestration.postgres

import io.availe.loadGradleProperties
import io.availe.orchestration.containers.domain.ContainerRunSpecification

internal data class PostgresConfig(
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String
)

internal fun loadPostgresConfig(): PostgresConfig {
    val props = loadGradleProperties()

    return PostgresConfig(
        dbUrl = props.getProperty("aimemory.db.url") ?: error("No aimemory.db.url found in gradle.properties"),
        dbUser = props.getProperty("aimemory.db.user") ?: error("No aimemory.db.user found in gradle.properties"),
        dbPassword = props.getProperty("aimemory.db.password")
            ?: error("No aimemory.db.password found in gradle.properties")
    )
}

internal fun postgresRunSpec(): ContainerRunSpecification<PostgresPortsList, PostgresVolumesList> {
    val config = loadPostgresConfig()
    val dbName = config.dbUrl.substringAfterLast('/')

    val environmentVariables = listOf(
        "POSTGRES_DB=$dbName",
        "POSTGRES_USER=${config.dbUser}",
        "POSTGRES_PASSWORD=${config.dbPassword}"
    )

    return ContainerRunSpecification(
        name = "availe-postgres",
        image = "pgvector/pgvector",
        tag = "pg18",
        environmentVariables = environmentVariables,
        portMappings = PostgresPortsList,
        volumes = PostgresVolumesList
    )
}