package cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.coroutines.runBlocking
import models.Review
import models.ReviewStatus
import service.ProjectOpener
import service.ReviewService

class ReviewsCommand : CliktCommand(name = "reviews", help = "List pending code reviews") {
    private val open by option("--open", "-o", help = "Open selected reviews in IDEA").flag()
    private val source by option("--source", "-s", help = "Filter by source")
        .choice("github", "space")

    override fun run() {
        runBlocking {
            val configManager = ConfigManager()
            val config = configManager.loadConfig()
            val reviewService = ReviewService(config)

            try {
                val reviews = reviewService.getReviews(source)
                    .filter { it.repository.lowercase().contains("jetbrains") }

                if (reviews.isEmpty()) {
                    echo("No pending reviews found! 🎉")
                    return@runBlocking
                }

                displayReviews(reviews)

                if (open) {
                    val projectOpener = ProjectOpener(config)
                    projectOpener.openReviewProjects(reviews)
                }
            } catch (e: Exception) {
                echo("Error fetching reviews: ${e.message}", err = true)
            }
        }
    }

    private fun displayReviews(reviews: List<Review>) {
        echo("📋 Code Reviews:")
        echo("=".repeat(50))

        reviews.groupBy { it.source }.forEach { (source, sourceReviews) ->
            echo("\n${source.name}:")
            sourceReviews.forEach { review ->
                val statusIcon = when (review.status) {
                    ReviewStatus.PENDING -> "⏳"
                    ReviewStatus.APPROVED -> "✅"
                    ReviewStatus.CHANGES_REQUESTED -> "🔄"
                    ReviewStatus.DRAFT -> "📝"
                }

                echo("  $statusIcon ${review.title}")
                echo("    📁 ${review.repository}")
                echo("    👤 ${review.author}")
                echo("    🔗 ${review.url}")
                echo()
            }
        }
    }
}