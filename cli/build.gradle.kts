group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":plugin"))
    implementation("org.apache.commons:commons-csv:1.9.0")
}

open class RunHeadlessIdeTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    @get:Input
    val projectsStorage: String? by project

    @get:Input
    val outputDir: String? by project

    init {
        jvmArgs = listOf("-Xms2G", "-Xmx5G", "-Djava.awt.headless=true")
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

tasks {
    register<RunHeadlessIdeTask>("runHeadlessIde") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            "pynose-headless",
            projectsStorage,
            outputDir
        )
    }
}
