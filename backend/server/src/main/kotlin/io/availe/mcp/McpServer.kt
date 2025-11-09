package io.availe.mcp

import com.xemantic.ai.tool.schema.mdc.mdcToolInput
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
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

internal class McpServer(private val port: Int) {
    fun start() {
        embeddedServer(Netty, port) {
            install(SSE)
            routing {
                mcp {
                    buildMcpServer()
                }
            }
        }.start(wait = false)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private fun buildMcpServer(): Server {
        return Server(
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

    @Serializable
    data class EchoArguments(
        val text: String,
    )

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
}