package io.availe

import io.availe.http4k.Http4kServer
import io.availe.mcp.McpServer

fun main() {
    val mainPort = 9000
    val mcpPort = 9001

    McpServer(mcpPort).start()
    Http4kServer(mainPort, mcpPort).start()
}