import cmds.ConfigCommand
import cmds.ConfigManager
import cmds.OpenCommand
import cmds.ReviewsCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import kotlinx.coroutines.runBlocking
import models.Review
import models.ReviewStatus
import service.ProjectOpener
import service.ReviewService

class WorkCommand : CliktCommand(name = "work", help = "Manage code reviews and open projects in IDEA") {
    init {
        subcommands(ReviewsCommand(), ConfigCommand(), OpenCommand())
    }

    override fun run() {
        if (currentContext.invokedSubcommand == null) {
            // Default behavior - show reviews and open projects
            runBlocking {
                val configManager = ConfigManager()
                val config = configManager.loadConfig()
                val reviewService = ReviewService(config)

                try {
                    val reviews = reviewService.getReviews(null)
                        .filter { it.repository.lowercase().contains("jetbrains") }
                    if (reviews.isEmpty()) {
                        echo("No pending reviews found! 🎉")
                        return@runBlocking
                    }

                    displayReviews(reviews)

                    val projectOpener = ProjectOpener(config)
                    projectOpener.openReviewProjects(reviews)
                } catch (e: Exception) {
                    echo("Error fetching reviews: ${e.message}", err = true)
                }
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

fun main(args: Array<String>) = WorkCommand().main(args)