package io.availe

import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.ui.redoc.redocWebjar
import org.http4k.format.Jackson
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Helidon
import org.http4k.server.asServer

val contract = contract {
    renderer = OpenApi3(ApiInfo("availe API", "v1"), Jackson)
    descriptionPath = "/openapi.json"
    routes += FileController().routes
}

val app = routes(
    "/api" bind contract,
    "/docs" bind redocWebjar {
        url = "/api/openapi.json"
        pageTitle = "availe API – Redoc"
        options["disable-search"] = "false"
    }
)

internal fun main() {
    app.asServer(Helidon(9000)).start()
}