package io.availe

import io.availe.ai.OllamaEmbeddingService
import io.availe.db.hikariSetup
import io.availe.graph.GraphService
import io.availe.memory.MemoryIngestionService
import io.availe.memory.MemoryRepository
import io.availe.server.Server
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration

fun main() {
    val port = 9002

    val embeddingService = OllamaEmbeddingService(
        baseUrl = System.getenv("OLLAMA_BASE_URL") ?: "http://localhost:11434",
        modelName = System.getenv("OLLAMA_MODEL") ?: "nomic-embed-text"
    )

    val dataSource = hikariSetup()
    val dsl: DSLContext = DSL.using(
        DefaultConfiguration()
            .set(dataSource)
            .set(SQLDialect.POSTGRES)
            .set(Settings().withReturnDefaultOnUpdatableRecord(true))
    )

    val memoryRepository = MemoryRepository(dsl)
    val memoryIngestionService = MemoryIngestionService(embeddingService, memoryRepository)
    val graphService = GraphService(memoryRepository)

    Server(port, dsl, memoryIngestionService, graphService).start()
}