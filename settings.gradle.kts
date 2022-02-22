rootProject.name = "pynose"
include(":plugin", ":cli")

val utilitiesRepo = "https://github.com/JetBrains-Research/plugin-utilities.git"
val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"

sourceControl {
    gitRepository(java.net.URI(utilitiesRepo)) {
        producesModule("$utilitiesProjectName:plugin-utilities-core")
        producesModule("$utilitiesProjectName:plugin-utilities-python")
        producesModule("$utilitiesProjectName:plugin-utilities-test")
    }
}