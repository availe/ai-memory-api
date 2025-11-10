package io.availe.api

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

internal fun Route.configureApiRoutes() {
    route("/health") {
        get {
            call.respond(HttpStatusCode.OK, "ok")
        }
    }

    route("/api") {
        post("/file") {
            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        part.dispose()
                    }

                    is PartData.FormItem -> {
                        // no-op
                    }

                    else -> part.dispose()
                }
            }
            call.respond(HttpStatusCode.Created)
        }
    }
}