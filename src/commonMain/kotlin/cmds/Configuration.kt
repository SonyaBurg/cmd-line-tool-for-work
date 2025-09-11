package cmds

import fileExists
import getHomeDirectory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import readFile
import writeFile

@Serializable
data class WorkConfig(
    val github: GitHubConfig? = null,
    val space: SpaceConfig? = null,
    val ideaPath: String = "idea",
    val projectsRoot: String = getHomeDirectory() + "/IdeaProjects"
)

@Serializable
data class GitHubConfig(
    val token: String,
    val username: String,
    val organizations: List<String> = emptyList()
)

@Serializable
data class SpaceConfig(
    val serverUrl: String,
    val token: String,
    val projectKey: String
)

class ConfigManager {
    private val configFile = getHomeDirectory() + "/.work-config.json"
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun loadConfig(): WorkConfig {
        return if (fileExists(configFile)) {
            json.decodeFromString<WorkConfig>(readFile(configFile))
        } else {
            WorkConfig()
        }
    }

    fun saveConfig(config: WorkConfig) {
        writeFile(configFile, json.encodeToString(config))
    }
}