package io.availe.utils

import io.availe.orchestration.postgres.PostgresConfig
import org.slf4j.LoggerFactory
import java.sql.DriverManager
import java.util.*

internal object ServiceWaiter {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun waitForPostgres(config: PostgresConfig, timeoutSeconds: Long = 60) {
        logger.info("Waiting for database connection at ${config.jdbcUrl}...")
        val deadline = System.currentTimeMillis() + timeoutSeconds * 1000
        val props = Properties().apply {
            setProperty("user", config.dbUser)
            setProperty("password", config.dbPassword)
        }
        var lastError: Throwable? = null

        while (System.currentTimeMillis() < deadline) {
            try {
                DriverManager.getConnection(config.jdbcUrl, props).use { conn ->
                    conn.createStatement().use { st ->
                        st.execute("SELECT 1")
                    }
                }
                logger.info("Database connection successful.")
                return
            } catch (t: Throwable) {
                lastError = t
                Thread.sleep(1_000)
            }
        }
        throw IllegalStateException("Database not ready after ${timeoutSeconds}s: ${lastError?.message}", lastError)
    }
}