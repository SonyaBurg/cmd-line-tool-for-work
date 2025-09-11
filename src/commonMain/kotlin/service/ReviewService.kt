package service

import api.GitHubClient
import api.SpaceClient
import cmds.WorkConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import models.Review

class ReviewService(private val config: WorkConfig) {

    suspend fun getReviews(sourceFilter: String?): List<Review> = coroutineScope {
        val reviews = mutableListOf<Review>()

        // Fetch GitHub reviews
        if ((sourceFilter == null || sourceFilter == "github") && config.github != null) {
            val githubClient = GitHubClient(config.github.token, config.github.username)
            try {
                val githubReviews = async { githubClient.getReviewRequests() }
                reviews.addAll(githubReviews.await())
            } finally {
                githubClient.close()
            }
        }

        // Fetch Space reviews
        if ((sourceFilter == null || sourceFilter == "space") && config.space != null) {
            val spaceClient = SpaceClient(
                config.space.serverUrl,
                config.space.token,
                config.space.projectKey
            )
            try {
                val spaceReviews = async { spaceClient.getReviewRequests() }
                reviews.addAll(spaceReviews.await())
            } finally {
                spaceClient.close()
            }
        }

        reviews.sortedBy { it.title }
    }
}