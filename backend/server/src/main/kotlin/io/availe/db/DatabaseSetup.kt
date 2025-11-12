package io.availe.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.availe.loadGradleProperties
import org.postgresql.PGProperty

internal fun hikariSetup(): HikariDataSource {
    val props = loadGradleProperties()

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = props.getProperty("db.url") ?: error("No db.url found")
        username = props.getProperty("db.user") ?: error("No db.user found")
        password = props.getProperty("db.password") ?: error("No db.password found")
        addDataSourceProperty(PGProperty.TCP_KEEP_ALIVE.getName(), true)
    }

    return HikariDataSource(hikariConfig)
}