group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":plugin"))
    implementation("org.apache.commons:commons-csv:1.9.0")
}

open class CliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    @get:Input
    val projectLocation: String? by project
    val outputDir: String? by project

    init {
        jvmArgs = listOf("-Xms2G", "-Xmx5G", "-Djava.awt.headless=true")
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

tasks {
    register<CliTask>("runCliHeadless") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            "cli",
            projectLocation,
            outputDir
        )
    }
}
