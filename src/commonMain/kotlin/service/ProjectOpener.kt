package service

import cmds.WorkConfig
import directoryExists
import executeShellCommand
import fileExists
import listDirectory
import models.Review

class ProjectOpener(private val config: WorkConfig) {

    fun openReviewProjects(reviews: List<Review>) {
        val projectPaths = reviews.mapNotNull { review ->
            findProjectPath(review.repository)
        }.distinct()

        if (projectPaths.isEmpty()) {
            println("No local projects found for the reviews")
            return
        }

        openProjectsInIdea(projectPaths)
    }

    private fun findProjectPath(repositoryName: String): String? {
        if (!directoryExists(config.projectsRoot)) {
            return null
        }

        // Try different naming conventions
        val possibleNames = listOf(
            repositoryName,
            repositoryName.substringAfterLast("/"), // For "org/repo" format
            repositoryName.replace("-", "_"),
            repositoryName.replace("_", "-")
        )

        val availableDirs = listDirectory(config.projectsRoot)

        for (name in possibleNames) {
            if (availableDirs.contains(name)) {
                val projectPath = "${config.projectsRoot}/$name"
                if (isValidProject(projectPath)) {
                    return projectPath
                }
            }
        }

        return null
    }

    private fun isValidProject(dirPath: String): Boolean {
        // Check for common project files
        val projectFiles = listOf(
            ".idea",
            "build.gradle.kts",
            "build.gradle",
            "pom.xml",
            "package.json"
        )

        return projectFiles.any {
            directoryExists("$dirPath/$it") || fileExists("$dirPath/$it")
        }
    }

    fun openProjectsInIdea(projectPaths: List<String>) {
        println("Opening ${projectPaths.size} project(s) in IntelliJ IDEA...")

        projectPaths.forEach { path ->
            val foundPath = names[path] ?: path
            val command = "idea \"$foundPath\""
            val success = executeShellCommand(command)

            val projectName = foundPath.substringAfterLast("/")
            if (success) {
                println("✓ Opened: $projectName")
            } else {
                println("✗ Failed to open: $projectName")
                println("  Make sure 'idea' command is available in your PATH")
                println("  You can add it via IntelliJ IDEA > Tools > Create Command-line Launcher")

            }
        }
    }

    companion object {
        val names = mapOf(
            "plugin" to "educational-plugin",
            "ss" to "educational-submissions",
            "submission-service" to "educational-submissions",
            "monorepo" to "ultimate",
            "lti" to "jetbrains-academy-lti",
            "lc" to "jetbrains-academy",
            "learning-center" to " jetbrains-academy",
        )
    }
}