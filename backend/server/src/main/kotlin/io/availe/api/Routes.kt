package io.availe.api

import io.availe.graph.GraphService
import io.availe.memory.MemoryIngestionService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@Serializable
data class TextIngestRequest(val text: String)

@Serializable
data class IngestResponse(val id: String)

@Serializable
data class CreateEdgeRequest(
    val sourceId: String,
    val targetId: String,
    val type: String
)

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
}

@Serializable
data class SearchResult(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val content: String,
    val score: Double
)

internal fun Route.configureApiRoutes(
    memoryIngestionService: MemoryIngestionService,
    graphService: GraphService
) {
    route("/health") {
        get {
            call.respond(HttpStatusCode.OK, "ok")
        }
    }

    route("/api") {
        route("/memory") {
            post("/text") {
                val request = call.receive<TextIngestRequest>()
                val id = memoryIngestionService.ingestText(request.text)
                call.respond(HttpStatusCode.Created, IngestResponse(id.toString()))
            }

            post("/file") {
                val multipart = call.receiveMultipart()
                var id: String? = null

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val filename = part.originalFileName ?: "unknown"
                        val bytes = part.provider().readRemaining().readByteArray()
                        id = memoryIngestionService.ingestFile(filename, bytes).toString()
                    }
                    part.dispose()
                }

                if (id != null) {
                    call.respond(HttpStatusCode.Created, IngestResponse(id!!))
                } else {
                    call.respond(HttpStatusCode.BadRequest, "No file provided")
                }
            }

            get("/search") {
                val query = call.request.queryParameters["q"]
                if (query.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing query parameter 'q'")
                    return@get
                }
                val results = graphService.search(query).map {
                    SearchResult(it.id, it.content, it.score)
                }
                call.respond(results)
            }
        }

        route("/graph") {
            get {
                call.respond(graphService.getGraphData())
            }

            post("/edges") {
                val req = call.receive<CreateEdgeRequest>()
                try {
                    graphService.createEdge(req.sourceId, req.targetId, req.type)
                    call.respond(HttpStatusCode.Created)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                }
            }
        }
    }
}