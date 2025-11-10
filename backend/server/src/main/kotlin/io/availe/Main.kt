package io.availe

import io.availe.http4k.Http4kServer
import io.availe.http4k.hikariSetup
import io.availe.mcp.McpServer
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration

fun main() {
    val mainPort = 9002
    val mcpPort = 9003

    val dataSource = hikariSetup()
    val dsl: DSLContext = DSL.using(
        DefaultConfiguration()
            .set(dataSource)
            .set(SQLDialect.POSTGRES)
            .set(Settings().withReturnDefaultOnUpdatableRecord(true))
    )

    McpServer(mcpPort).start()
    Http4kServer(mainPort, mcpPort).start()
}