package io.availe.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.postgresql.PGProperty

internal fun hikariSetup(): HikariDataSource {
    val props = loadGradleProperties()

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = props.getProperty("aimemory.db.url") ?: error("No aimemory.db.url found")
        username = props.getProperty("aimemory.db.user") ?: error("No aimemory.db.user found")
        password = props.getProperty("aimemory.db.password") ?: error("No aimemory.db.password found")
        addDataSourceProperty(PGProperty.TCP_KEEP_ALIVE.getName(), true)
    }

    return HikariDataSource(hikariConfig)
}