package cmds
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice

class ConfigCommand : CliktCommand(name = "config", help = "Configure GitHub and Space credentials") {
    private val service by option("--service", "-s", help = "Service to configure")
        .choice("github", "space", "idea")
    private val show by option("--show", help = "Show current configuration").flag()

    override fun run() {
        val configManager = ConfigManager()
        var config = configManager.loadConfig()

        if (show) {
            showConfig(config)
            return
        }

        when (service) {
            "github" -> config = configureGitHub(config)
            "space" -> config = configureSpace(config)
            "idea" -> config = configureIdea(config)
            else -> {
                echo("Available services: github, space, idea")
                echo("Use --show to see current configuration")
                return
            }
        }

        configManager.saveConfig(config)
        echo("Configuration saved!")
    }

    private fun showConfig(config: WorkConfig) {
        echo("Current configuration:")
        echo("GitHub: ${if (config.github != null) "✓ Configured" else "✗ Not configured"}")
        echo("Space: ${if (config.space != null) "✓ Configured" else "✗ Not configured"}")
        echo("IDEA path: ${config.ideaPath}")
        echo("Projects root: ${config.projectsRoot}")
    }

    private fun configureGitHub(config: WorkConfig): WorkConfig {
        val token = terminal.prompt("GitHub personal access token", hideInput = true) ?: ""
        val username = terminal.prompt("GitHub username") ?: ""
        val orgs = terminal.prompt("Organizations (comma-separated, optional)", default = "") ?: ""

        val organizations = if (orgs.isBlank()) emptyList() else orgs.split(",").map { it.trim() }

        return config.copy(github = GitHubConfig(token, username, organizations))
    }

    private fun configureSpace(config: WorkConfig): WorkConfig {
        val serverUrl = terminal.prompt("Space server URL (e.g., https://yourorg.jetbrains.space)") ?: ""
        val token = terminal.prompt("Space token", hideInput = true) ?: ""
        val projectKey = terminal.prompt("Space project key") ?: ""

        return config.copy(space = SpaceConfig(serverUrl, token, projectKey))
    }

    private fun configureIdea(config: WorkConfig): WorkConfig {
        val ideaPath = terminal.prompt("Path to IntelliJ IDEA executable", default = config.ideaPath) ?: config.ideaPath
        val projectsRoot = terminal.prompt("Projects root directory", default = config.projectsRoot) ?: config.projectsRoot

        return config.copy(ideaPath = ideaPath, projectsRoot = projectsRoot)
    }
}