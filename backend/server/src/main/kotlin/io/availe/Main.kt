package io.availe

import io.availe.db.hikariSetup
import io.availe.server.Server
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration

fun main() {
    val port = 9002

    val dataSource = hikariSetup()
    val dsl: DSLContext = DSL.using(
        DefaultConfiguration()
            .set(dataSource)
            .set(SQLDialect.POSTGRES)
            .set(Settings().withReturnDefaultOnUpdatableRecord(true))
    )

    Server(port, dsl).start()
}