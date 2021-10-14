group = "org.jetbrains.research.pynose"
version = "1.0-SNAPSHOT"

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
    }

    intellij {
        type.set("PC")
        version.set("PC-2021.1.3")
        updateSinceUntilBuild.set(false)
        plugins.set(listOf("PythonCore:211.7628.24"))
    }

    tasks.withType<org.jetbrains.intellij.tasks.BuildSearchableOptionsTask>()
        .forEach { it.enabled = false }

    tasks.getByName<Test>("test") {
        System.setProperty("idea.home.path", "/home/oleg/.local/share/JetBrains/Toolbox/apps/IDEA-U/ch-0/212.5284.40")
    }
}

