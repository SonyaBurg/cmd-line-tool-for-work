
expect fun executeShellCommand(command: String?): Boolean
expect fun directoryExists(path: String): Boolean
expect fun listDirectory(path: String): List<String>

expect fun getHomeDirectory(): String
expect fun fileExists(path: String): Boolean
expect fun readFile(path: String): String
expect fun writeFile(path: String, content: String)