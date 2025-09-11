package api


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import models.Review
import models.ReviewSource
import models.ReviewStatus

@Serializable
data class SpaceMergeRequest(
    val id: String,
    val number: Int,
    val title: String,
    val url: String,
    val state: String,
    val author: SpaceProfile,
    val repository: SpaceRepository
)

@Serializable
data class SpaceProfile(
    val username: String
)

@Serializable
data class SpaceRepository(
    val name: String
)

class SpaceClient(
    private val serverUrl: String,
    private val token: String,
    private val projectKey: String
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getReviewRequests(): List<Review> {
        // Note: This is a simplified implementation
        // Space API structure may vary based on your organization's setup
        try {
            val mergeRequests: List<SpaceMergeRequest> = client.get("$serverUrl/api/http/projects/$projectKey/code-reviews") {
                headers {
                    append("Authorization", "Bearer $token")
                    append("Accept", "application/json")
                }
            }.body()

            return mergeRequests.map { mr ->
                Review(
                    id = mr.id,
                    title = mr.title,
                    url = mr.url,
                    repository = mr.repository.name,
                    author = mr.author.username,
                    status = when (mr.state) {
                        "Open" -> ReviewStatus.PENDING
                        "Closed" -> ReviewStatus.APPROVED
                        else -> ReviewStatus.DRAFT
                    },
                    source = ReviewSource.SPACE
                )
            }
        } catch (e: Exception) {
            println("Warning: Could not fetch Space reviews: ${e.message}")
            return emptyList()
        }
    }

    fun close() {
        client.close()
    }
}