group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":core"))
    implementation(project(":plugin"))
}

tasks {
    runIde {
        val myPath: String? by project
        val myPython: String? by project
        val myOutDir: String? by project
        args = listOfNotNull(
            "pynose",
            myPath ?: "",
            myPython ?: "",
            myOutDir ?: ""
        )
        jvmArgs = listOf("-Djava.awt.headless=true")
    }
}