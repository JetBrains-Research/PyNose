package org.jetbrains.research.pynose.headless

import com.google.gson.JsonArray
import com.intellij.codeInspection.InspectionManager
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.pluginUtilities.sdk.setSdkToProject
import org.jetbrains.research.pynose.headless.inspections.HeadlessInspectionAnalyzer
import java.io.File
import kotlin.system.exitProcess


class HeadlessRunner : ApplicationStarter {

    override fun getCommandName() = "pynose-headless"
    private val inspectionsAnalyzer = HeadlessInspectionAnalyzer()

    override fun main(args: List<String>) {
        if (args.size < 2) {
            System.err.println("Specify project and output paths as arguments")
            exitProcess(1)
        }
        val repoRoot = File(args[1])
        var counter = 1
        repoRoot.listFiles()?.forEach {
            inspectionsAnalyzer.resetParameters()
            val projectRoot = it.path
            val jsonUnittestProjectResult = JsonArray()
            val jsonPytestProjectResult = JsonArray()
            var projectName = ""
            val outputDir = args[2]
            ApplicationManager.getApplication().invokeAndWait {
                val project = ProjectUtil.openOrImport(projectRoot, null, true) ?: return@invokeAndWait
                projectName = project.name
                println("Processing repo #$counter: $projectName")
                setSdkToProject(project, projectRoot.toString())
                val inspectionManager = InspectionManager.getInstance(project)
                WriteCommandAction.runWriteCommandAction(project) {
                    inspectionsAnalyzer.analyse(project, inspectionManager, jsonUnittestProjectResult, jsonPytestProjectResult)
                }
            }
            inspectionsAnalyzer.processFileOutput(projectName, outputDir, jsonUnittestProjectResult, jsonPytestProjectResult)
            println("Finished processing repo #$counter: $projectName\n")
            counter++
        }
        exitProcess(0)
    }
}
