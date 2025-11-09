package io.availe.http4k

import org.http4k.client.JavaHttpClient
import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.ui.redocLite
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Helidon
import org.http4k.server.asServer

internal class Http4kServer(private val port: Int, private val mcpPort: Int) {
    fun start() {
        app.asServer(Helidon(port)).start()
    }

    private val contract = contract {
        renderer = OpenApi3(ApiInfo("availe API", "v1"), Jackson)
        descriptionPath = "/openapi.json"
        routes += FileController().routes
    }

    private val redoc = redocLite {
        url = "/api/openapi.json"
        pageTitle = "availe API – Redoc"
        options["disable-search"] = "false"
    }


    private fun mcpProxy(mcpPort: Int): HttpHandler {
        val client = JavaHttpClient(responseBodyMode = BodyMode.Stream)

        return { req ->
            val targetPath = req.uri.path.removePrefix("/mcp").ifEmpty { "/" }
            val target = Uri.of("http://localhost:$mcpPort$targetPath")
                .query(req.uri.query)

            val streamingRequest = req
                .uri(target)
                .removeHeader("Accept-Encoding")
                .removeHeader("Host")

            val response = client(streamingRequest)
            response
        }
    }

    private val app = routes(
        "/health" bind Method.GET to { Response(OK).body("ok") },
        "/api" bind contract,
        "/mcp" bind routes(
            "/{rest:.*}" bind mcpProxy(mcpPort),
            "/" bind mcpProxy(mcpPort),
            "" bind mcpProxy(mcpPort)
        ),
        redoc
    )
}