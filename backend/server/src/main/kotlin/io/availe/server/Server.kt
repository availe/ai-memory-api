package io.availe.server

import com.xemantic.ai.tool.schema.mdc.mdcToolInput
import io.availe.api.configureApiRoutes
import io.availe.memory.MemoryIngestionService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.jooq.DSLContext

internal class Server(
    private val port: Int,
    private val dsl: DSLContext,
    private val memoryIngestionService: MemoryIngestionService
) {
    fun start() {
        embeddedServer(Netty, port = port) {
            module(dsl, memoryIngestionService)
        }.start(wait = true)
    }
}

private fun Application.module(dsl: DSLContext, memoryIngestionService: MemoryIngestionService) {
    install(SSE)
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }

    routing {
        configureApiRoutes(memoryIngestionService)
        configureMcp()
    }
}

private fun Routing.configureMcp() {
    mcp {
        Server(
            serverInfo = Implementation("availe-mcp-server", "1.0.0"),
            options = ServerOptions(
                capabilities = ServerCapabilities(
                    resources = ServerCapabilities.Resources(subscribe = true, listChanged = true),
                    prompts = ServerCapabilities.Prompts(listChanged = true),
                    tools = ServerCapabilities.Tools(listChanged = true)
                )
            )
        ).apply {
            addEchoTool()
        }
    }
}

@Serializable
data class EchoArguments(val text: String)

private val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

private fun Server.addEchoTool() {
    addTool(
        name = "echo",
        description = "Echoes the provided text back.",
        inputSchema = mdcToolInput<EchoArguments>()
    ) { req ->
        val args = json.decodeFromJsonElement<EchoArguments>(req.arguments)
        val output = args.text.ifBlank { "(empty)" }
        CallToolResult(content = listOf(TextContent(output)))
    }
}