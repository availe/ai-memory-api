package io.availe.orchestration.postgres

import io.availe.loadGradleProperties
import io.availe.orchestration.containers.domain.ContainerRunSpecification

internal data class PostgresConfig(
    val dbHost: String,
    val dbPort: Int,
    val dbUser: String,
    val dbPassword: String,
    val dbName: String
) {
    val jdbcUrl: String
        get() = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
}

internal fun loadPostgresConfig(): PostgresConfig {
    val props = loadGradleProperties()
    val dbName = "ai_memory_api_db"

    val dbUrl = props.getProperty("db.url") ?: error("No db.url found in gradle.properties")
    val dbUser = props.getProperty("db.user") ?: error("No db.user found in gradle.properties")
    val dbPassword = props.getProperty("db.password") ?: error("No db.password found in gradle.properties")

    val host = dbUrl.substringAfter("//").substringBefore(":")

    return PostgresConfig(
        dbHost = host,
        dbPort = PostgresPortsList.Postgres.hostPortNumber,
        dbUser = dbUser,
        dbPassword = dbPassword,
        dbName = dbName
    )
}

internal fun postgresRunSpec(): ContainerRunSpecification<PostgresPortsList, PostgresVolumesList> {
    val config = loadPostgresConfig()

    val environmentVariables = listOf(
        "POSTGRES_DB=${config.dbName}",
        "POSTGRES_USER=${config.dbUser}",
        "POSTGRES_PASSWORD=${config.dbPassword}"
    )

    return ContainerRunSpecification(
        name = "ai-memory-api-postgres",
        image = "pgvector/pgvector",
        tag = "pg18",
        environmentVariables = environmentVariables,
        portMappings = PostgresPortsList,
        volumes = PostgresVolumesList
    )
}