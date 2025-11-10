package io.availe.ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface EmbeddingService {
    suspend fun embed(text: String): List<Float>
}

internal class OllamaEmbeddingService(
    private val baseUrl: String = "http://localhost:11434",
    private val modelName: String = "nomic-embed-text"
) : EmbeddingService {

    private val client = HttpClient(Java) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
        }
    }

    override suspend fun embed(text: String): List<Float> {
        val response = client.post("$baseUrl/api/embeddings") {
            contentType(ContentType.Application.Json)
            setBody(OllamaRequest(model = modelName, prompt = text))
        }
        return response.body<OllamaResponse>().embedding
    }

    @Serializable
    private data class OllamaRequest(
        val model: String,
        val prompt: String
    )

    @Serializable
    private data class OllamaResponse(
        val embedding: List<Float>
    )
}