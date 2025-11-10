package io.availe.api

import io.availe.memory.MemoryIngestionService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class TextIngestRequest(val text: String)

@Serializable
data class IngestResponse(val id: String)

internal fun Route.configureApiRoutes(memoryIngestionService: MemoryIngestionService) {
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
                        val bytes = part.streamProvider().readBytes()
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
        }
    }
}