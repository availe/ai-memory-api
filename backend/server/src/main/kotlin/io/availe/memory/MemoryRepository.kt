package io.availe.memory

import com.pgvector.PGvector
import io.availe.db.jooq.tables.references.MEMORIES
import io.availe.db.jooq.tables.references.MEMORY_EDGES
import org.jooq.DSLContext
import org.jooq.JSONB
import java.time.OffsetDateTime
import java.util.*

internal class MemoryRepository(private val dslContext: DSLContext) {
    fun save(
        content: String,
        embedding: List<Float>,
        sourceType: SourceType,
        metadata: String = "{}"
    ): UUID {
        return dslContext.connectionResult { connection ->
            PGvector.addVectorType(connection)
            val sql = """
                insert into ai_memory_api.memories (content, embedding, source_type, metadata)
                values (?, ?, ?, ?::jsonb)
                returning id
            """.trimIndent()

            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, content)
                preparedStatement.setObject(2, PGvector(embedding.toFloatArray()))
                preparedStatement.setString(3, sourceType.name)
                preparedStatement.setString(4, metadata)

                val resultSet = preparedStatement.executeQuery()
                if (resultSet.next()) {
                    resultSet.getObject(1, UUID::class.java)
                } else {
                    error("Failed to insert memory, no ID returned")
                }
            }
        }
    }

    fun findSimilar(embedding: List<Float>, limit: Int = 5, threshold: Double = 0.7): List<ScoredMemory> {
        return dslContext.connectionResult { connection ->
            PGvector.addVectorType(connection)
            val sql = """
                select id, content, (1 - (embedding <=> ?)) as score
                from ai_memory_api.memories
                where (1 - (embedding <=> ?)) >= ?
                and (metadata ->> 'isParent' IS NULL)
                order by score desc
                limit ?
            """.trimIndent()

            connection.prepareStatement(sql).use { preparedStatement ->
                val vector = PGvector(embedding.toFloatArray())
                preparedStatement.setObject(1, vector)
                preparedStatement.setObject(2, vector)
                preparedStatement.setDouble(3, threshold)
                preparedStatement.setInt(4, limit)

                val resultSet = preparedStatement.executeQuery()
                val results = mutableListOf<ScoredMemory>()
                while (resultSet.next()) {
                    results.add(
                        ScoredMemory(
                            id = resultSet.getObject("id", UUID::class.java),
                            content = resultSet.getString("content"),
                            score = resultSet.getDouble("score")
                        )
                    )
                }
                results
            }
        }
    }

    fun createEdge(sourceId: UUID, targetId: UUID, relationship: RelationshipType) {
        dslContext.insertInto(MEMORY_EDGES)
            .set(MEMORY_EDGES.SOURCE_ID, sourceId)
            .set(MEMORY_EDGES.TARGET_ID, targetId)
            .set(MEMORY_EDGES.RELATIONSHIP, relationship.name)
            .onDuplicateKeyUpdate()
            .set(MEMORY_EDGES.RELATIONSHIP, relationship.name)
            .execute()
    }

    fun findAllMemories(): List<MemoryRecord> {
        return dslContext.select(
            MEMORIES.ID,
            MEMORIES.CONTENT,
            MEMORIES.SOURCE_TYPE,
            MEMORIES.METADATA,
            MEMORIES.CREATED_AT
        )
            .from(MEMORIES)
            .fetch { r ->
                MemoryRecord(
                    id = r[MEMORIES.ID]!!,
                    content = r[MEMORIES.CONTENT]!!,
                    sourceType = SourceType.valueOf(r[MEMORIES.SOURCE_TYPE]!!),
                    metadata = r[MEMORIES.METADATA]!!,
                    createdAt = r[MEMORIES.CREATED_AT]!!
                )
            }
    }

    fun findAllEdges(): List<EdgeRecord> {
        return dslContext.select(MEMORY_EDGES.SOURCE_ID, MEMORY_EDGES.TARGET_ID, MEMORY_EDGES.RELATIONSHIP)
            .from(MEMORY_EDGES)
            .fetch { r ->
                EdgeRecord(
                    sourceId = r[MEMORY_EDGES.SOURCE_ID]!!,
                    targetId = r[MEMORY_EDGES.TARGET_ID]!!,
                    relationship = RelationshipType.valueOf(r[MEMORY_EDGES.RELATIONSHIP]!!)
                )
            }
    }

    data class ScoredMemory(val id: UUID, val content: String, val score: Double)
    data class MemoryRecord(
        val id: UUID,
        val content: String,
        val sourceType: SourceType,
        val metadata: JSONB,
        val createdAt: OffsetDateTime
    )

    data class EdgeRecord(val sourceId: UUID, val targetId: UUID, val relationship: RelationshipType)

    enum class SourceType {
        TEXT,
        FILE
    }

    enum class RelationshipType {
        EXTEND,
        UPDATE,
        DERIVE,
        BELONGS_TO
    }
}