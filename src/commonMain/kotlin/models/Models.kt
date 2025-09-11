package models

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    val title: String,
    val url: String,
    val repository: String,
    val author: String,
    val status: ReviewStatus,
    val source: ReviewSource,
    val projectPath: String? = null
)

enum class ReviewStatus {
    PENDING, APPROVED, CHANGES_REQUESTED, DRAFT
}

enum class ReviewSource {
    GITHUB, SPACE
}

@Serializable
data class Project(
    val name: String,
    val path: String,
    val repository: String
)