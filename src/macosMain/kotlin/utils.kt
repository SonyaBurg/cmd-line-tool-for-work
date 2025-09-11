import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.Foundation.*
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
actual fun directoryExists(path: String): Boolean {
    val exists = NSFileManager.defaultManager.fileExistsAtPath(path)
    return exists
}

@OptIn(ExperimentalForeignApi::class)
actual fun listDirectory(path: String): List<String> {
    val fileManager = NSFileManager.defaultManager
    val contents = fileManager.contentsOfDirectoryAtPath(path, error = null)
    return (contents as? List<String>) ?: emptyList()
}

@OptIn(ExperimentalForeignApi::class)
actual fun getHomeDirectory(): String {
    return getenv("HOME")?.toKString() ?: "/Users/$(whoami)"
}

actual fun fileExists(path: String): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(path)
}

@OptIn(ExperimentalForeignApi::class)
actual fun readFile(path: String): String {
    return NSString.stringWithContentsOfFile(
        path = path,
        encoding = NSUTF8StringEncoding,
        error = null
    ) ?: ""
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun writeFile(path: String, content: String) {
    NSString.create(string = content).writeToFile(
        path = path,
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null
    )

}

@OptIn(ExperimentalForeignApi::class)
actual fun executeShellCommand(command: String?): Boolean {
    val process = NSTask()
    process.setLaunchPath("/bin/sh")
    process.setArguments(listOf("-c", command))

    return try {
        process.launch()
        process.waitUntilExit()
        val success = process.terminationStatus == 0
        if (success) {
            println("✓ Executed: $command")
        } else {
            println("✗ Failed to execute: $command (exit code: ${process.terminationStatus})")
        }
        success
    } catch (e: Exception) {
        println("✗ Error executing: $command - ${e.message}")
        false
    }
}

