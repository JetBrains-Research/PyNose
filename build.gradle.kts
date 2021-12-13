group = "org.jetbrains.research.pynose"
version = "1.0.0"

val utilitiesProjectName = "org.jetbrains.research.pluginUtilities"

plugins {
    java
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("org.jetbrains.intellij") version "1.1.2"
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("org.jetbrains.intellij")
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
        implementation("org.apache.opennlp:opennlp-tools:1.9.3")
        implementation("com.google.code.gson:gson:2.8.8")
        implementation("$utilitiesProjectName:plugin-utilities-core") {
            version {
                branch = "main"
            }
        }
        implementation("$utilitiesProjectName:plugin-utilities-python") {
            version {
                branch = "main"
            }
        }
        implementation("$utilitiesProjectName:plugin-utilities-test") {
            version {
                branch = "main"
            }
        }
        testImplementation("io.mockk:mockk:1.10.2")
    }

    intellij {
        type.set("PC")
        version.set("PC-2021.3")
        updateSinceUntilBuild.set(false)
        plugins.set(listOf("PythonCore:213.5744.248"))
    }

    tasks.withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
        .forEach { it.enabled = false }
}

