package api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import models.Review
import models.ReviewSource
import models.ReviewStatus

@Serializable
data class GitHubSearchResponse(
    val items: List<GitHubPullRequest>
)

@Serializable
data class GitHubPullRequest(
    val id: Long,
    val number: Int,
    val title: String,
    @SerialName("html_url") val htmlUrl: String,
    val user: GitHubUser,
    val state: String,
    val draft: Boolean,
    @SerialName("repository_url") val repositoryUrl: String? = null
)

@Serializable
data class GitHubUser(
    val login: String
)

class GitHubClient(private val token: String, private val username: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getReviewRequests(): List<Review> {
        val reviews = mutableListOf<Review>()

        // Get PRs where user is requested as reviewer
        val reviewRequests: GitHubSearchResponse = client.get("https://api.github.com/search/issues") {
            headers {
                append("Authorization", "Bearer $token")
                append("Accept", "application/vnd.github.v3+json")
            }
            parameter("q", "type:pr state:open review-requested:$username")
        }.body()

        // Get PRs created by user
        val userPrs: GitHubSearchResponse = client.get("https://api.github.com/search/issues") {
            headers {
                append("Authorization", "Bearer $token")
                append("Accept", "application/vnd.github.v3+json")
            }
            parameter("q", "type:pr state:open author:$username")
        }.body()

        reviews.addAll(reviewRequests.items.map { it.toReview() })
        reviews.addAll(userPrs.items.map { it.toReview() })

        return reviews.distinctBy { it.id }
    }

    private fun GitHubPullRequest.toReview(): Review {
        val repoName = repositoryUrl?.substringAfterLast("/") ?: "unknown"
        return Review(
            id = id.toString(),
            title = title,
            url = htmlUrl,
            repository = repoName,
            author = user.login,
            status = when {
                draft -> ReviewStatus.DRAFT
                state == "open" -> ReviewStatus.PENDING
                else -> ReviewStatus.APPROVED
            },
            source = ReviewSource.GITHUB
        )
    }

    fun close() {
        client.close()
    }
}