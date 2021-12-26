group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":core"))
    implementation(project(":plugin"))
}

open class CliTask : org.jetbrains.intellij.tasks.RunIdeTask() {
    @get:Input
    val projectLocation: String? by project

    init {
        jvmArgs = listOf("-Djava.awt.headless=true")
        standardInput = System.`in`
        standardOutput = System.`out`
    }
}

tasks {
    register<CliTask>("runCliHeadless") {
        dependsOn("buildPlugin")
        args = listOfNotNull(
            "cli",
            projectLocation
        )
    }
}
