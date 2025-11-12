package io.availe.provisioning

import io.availe.orchestration.postgres.PostgresConfig
import org.slf4j.LoggerFactory
import java.sql.DriverManager

internal class PostgresProvisioner(private val config: PostgresConfig) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun provision() {
        logger.info("Provisioning Postgres...")
        enableVectorExtension()
    }

    private fun enableVectorExtension() {
        val sql = "CREATE EXTENSION IF NOT EXISTS vector"
        try {
            DriverManager.getConnection(config.jdbcUrl, config.dbUser, config.dbPassword).use { conn ->
                conn.createStatement().use { st ->
                    st.execute(sql)
                }
            }
            logger.info("Extension 'vector' enabled or already exists.")
        } catch (t: Throwable) {
            logger.error("Failed to enable 'vector' extension: ${t.message}", t)
            throw IllegalStateException("Could not enable extension 'vector'", t)
        }
    }
}