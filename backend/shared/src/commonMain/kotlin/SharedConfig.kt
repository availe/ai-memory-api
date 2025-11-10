package io.availe

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
object SharedConfig {
    val hostIp: String = NetworkConstants.HOST_IP
    val apiUrl: String = "http://${NetworkConstants.HOST_IP}:9002"
}