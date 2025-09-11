package cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import service.ProjectOpener
import directoryExists

class OpenCommand : CliktCommand(name = "open", help = "Open specific projects in IDEA") {
    private val projects by argument("projects", help = "Project names to open").multiple()

    override fun run() {
        val configManager = ConfigManager()
        val config = configManager.loadConfig()
        val projectOpener = ProjectOpener(config)

        if (projects.isEmpty()) {
            echo("Please specify project names to open")
            return
        }

        val projectPaths = projects.mapNotNull { alias ->
            val projectName = ProjectOpener.names[alias] ?: alias
            val path = "${config.projectsRoot}/$projectName"
            if (directoryExists(path)) {
                path
            } else {
                echo("Project not found: $projectName")
                null
            }
        }

        if (projectPaths.isNotEmpty()) {
            projectOpener.openProjectsInIdea(projectPaths)
        }
    }
}