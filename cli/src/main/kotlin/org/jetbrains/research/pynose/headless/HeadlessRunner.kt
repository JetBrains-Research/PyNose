package org.jetbrains.research.pynose.headless

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.descendants
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pluginUtilities.sdk.setSdkToProject
import org.jetbrains.research.pynose.headless.io.csv.CsvFilesHandler
import org.jetbrains.research.pynose.headless.io.json.JsonFilesHandler
import org.jetbrains.research.pynose.plugin.inspections.TestRunner
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import java.io.File
import kotlin.system.exitProcess


class HeadlessRunner : ApplicationStarter {

    override fun getCommandName() = "pynose-headless"

    private var mode = TestRunner.UNKNOWN
    private var unittestFileCount = 0
    private var pytestFileCount = 0
    private var unittestCsvMap: MutableMap<String, MutableSet<PsiFile>> = mutableMapOf()
    private var pytestCsvMap: MutableMap<String, MutableSet<PsiFile>> = mutableMapOf()
    private var aggregatedPytestHasHeader = false
    private var aggregatedUnittestHasHeader = false
    private var agCsvPytestData: MutableList<MutableList<String>> = mutableListOf()
    private var agCsvUnittestData: MutableList<MutableList<String>> = mutableListOf()
    private val separator = File.separatorChar
    private val jsonHandler = JsonFilesHandler()
    private val csvHandler = CsvFilesHandler()

    override fun main(args: List<String>) {
        if (args.size < 2) {
            System.err.println("Specify project and output paths as arguments")
            exitProcess(0) // todo
        }
        val repoRoot = File(args[1])
        var counter = 1
        repoRoot.listFiles()?.forEach {
            unittestCsvMap = mutableMapOf()
            pytestCsvMap = mutableMapOf()
            unittestFileCount = 0
            pytestFileCount = 0
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
                    analyse(project, inspectionManager, jsonUnittestProjectResult, jsonPytestProjectResult)
                }
            }
            processFileOutput(projectName, outputDir, jsonUnittestProjectResult, jsonPytestProjectResult)
            println("Finished processing repo #$counter: $projectName\n")
            counter++
        }
        exitProcess(0)
    }

    private fun processFileOutput(
        projectName: String,
        outputDir: String,
        jsonUnittestProjectResult: JsonArray,
        jsonPytestProjectResult: JsonArray
    ) {
        val jsonUnittestFile = jsonHandler.initOutputJsonFile("$outputDir${separator}unittest", projectName)
        val jsonPytestFile = jsonHandler.initOutputJsonFile("$outputDir${separator}pytest", projectName)
        csvHandler.writeToCsvFile(
            outputDir, projectName, unittestCsvMap, unittestFileCount,
            aggregatedUnittestHasHeader, agCsvUnittestData, "unittest"
        )
        csvHandler.writeToCsvFile(
            outputDir, projectName, pytestCsvMap, pytestFileCount,
            aggregatedPytestHasHeader, agCsvPytestData, "pytest"
        )
        aggregatedUnittestHasHeader = true
        aggregatedPytestHasHeader = true
        jsonHandler.writeToJsonFile(jsonUnittestProjectResult, jsonUnittestFile)
        jsonHandler.writeToJsonFile(jsonPytestProjectResult, jsonPytestFile)
        csvHandler.writeToAggregatedCsv(outputDir, "pytest", agCsvPytestData)
        csvHandler.writeToAggregatedCsv(outputDir, "unittest", agCsvUnittestData)
    }

    private fun getFiles(project: Project): List<Array<PsiFile>> {
        return FilenameIndex.getAllFilesByExt(project, "py", GlobalSearchScope.projectScope(project))
            .filter { it.name.startsWith("test") || it.name.endsWith("test.py") }
            .map { FilenameIndex.getFilesByName(project, it.name, GlobalSearchScope.projectScope(project)) }
    }

    private fun initParams(
        inspectionManager: InspectionManager,
        psiFile: PsiFile,
        inspection: PyInspection
    ): Pair<ProblemsHolder, PsiElementVisitor> {
        val holder = ProblemsHolder(inspectionManager, psiFile, false)
        val session = LocalInspectionToolSession(psiFile, 0, psiFile.textLength)
        val inspectionVisitor = inspection.buildVisitor(holder, false, session)
        return Pair(holder, inspectionVisitor)
    }

    private fun analyse(
        project: Project,
        inspectionManager: InspectionManager,
        jsonUnittestProjectResult: JsonArray,
        jsonPytestProjectResult: JsonArray
    ) {
        val files = getFiles(project)
        files.forEach {
            it.forEach { psiFile ->
                val jsonFileResult = JsonObject()
                jsonFileResult.addProperty("Filename", psiFile.name)
                val jsonFileResultArray = JsonArray()
                val testRunner = project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(psiFile)
                mode = testRunner
                if (testRunner == TestRunner.PYTEST) {
                    pytestFileCount++
                    analysePytest(inspectionManager, psiFile, jsonFileResultArray)
                    analyseUniversal(inspectionManager, psiFile, jsonFileResultArray)
                    jsonFileResult.add("Results for file", jsonFileResultArray)
                    jsonPytestProjectResult.add(jsonFileResult)
                } else if (testRunner == TestRunner.UNITTESTS) {
                    unittestFileCount++
                    analyseUnittest(inspectionManager, psiFile, jsonFileResultArray)
                    analyseUniversal(inspectionManager, psiFile, jsonFileResultArray)
                    jsonFileResult.add("Results for file", jsonFileResultArray)
                    jsonUnittestProjectResult.add(jsonFileResult)
                }
            }
        }
    }

    private fun analysePytestOnRequiredLevel(
        inspectionManager: InspectionManager,
        psiFile: PsiFile,
        jsonFileResultArray: JsonArray,
        inspection: PyInspection,
        inspectionName: String,
        gatheringFunction: (String, ProblemsHolder, JsonArray) -> Unit
    ) {
        val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
        psiFile.accept(inspectionVisitor)
        gatheringFunction(inspectionName, holder, jsonFileResultArray)
        csvHandler.gatherCsvInformation(inspectionName, holder, psiFile, pytestCsvMap)
    }

    private fun analysePytest(inspectionManager: InspectionManager, psiFile: PsiFile, resultArray: JsonArray) {
        HeadlessInspectionsUtils.getPytestInspectionsFunctionLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyFunction::class.java).forEach {
                it.accept(inspectionVisitor)
                it.descendants { true }.forEach { d -> d.accept(inspectionVisitor) }
            }
            jsonHandler.gatherJsonFunctionInformation(inspectionName, holder, resultArray)
            csvHandler.gatherCsvInformation(inspectionName, holder, psiFile, pytestCsvMap)
        }
        HeadlessInspectionsUtils.getPytestInspectionsFileLaunchLevel().forEach { (inspection, inspectionName) ->
            analysePytestOnRequiredLevel(
                inspectionManager, psiFile, resultArray,
                inspection, inspectionName, jsonHandler::gatherJsonFunctionInformation,
            )
        }
        HeadlessInspectionsUtils.getPytestInspectionsFileResultLevel().forEach { (inspection, inspectionName) ->
            analysePytestOnRequiredLevel(
                inspectionManager, psiFile, resultArray,
                inspection, inspectionName, jsonHandler::gatherJsonClassOrFileInformation
            )
        }
    }

    private fun analyseUnittest(
        inspectionManager: InspectionManager, psiFile: PsiFile, jsonFileResultArray: JsonArray
    ) {
        HeadlessInspectionsUtils.getUnittestInspectionsFunctionResultLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java).forEach {
                it.accept(inspectionVisitor)
                it.descendants { true }.forEach { d -> d.accept(inspectionVisitor) }
            }
            jsonHandler.gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
            csvHandler.gatherCsvInformation(inspectionName, holder, psiFile, unittestCsvMap)
        }
        HeadlessInspectionsUtils.getUnittestInspectionsClassResultLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java).forEach { it.accept(inspectionVisitor) }
            jsonHandler.gatherJsonClassOrFileInformation(inspectionName, holder, jsonFileResultArray)
            csvHandler.gatherCsvInformation(inspectionName, holder, psiFile, unittestCsvMap)
        }
    }

    private fun analyseUniversal(
        inspectionManager: InspectionManager, psiFile: PsiFile, jsonFileResultArray: JsonArray
    ) {
        HeadlessInspectionsUtils.getUniversalInspections().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyCallExpression::class.java).forEach {
                it.accept(inspectionVisitor)
            }
            jsonHandler.gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
            if (mode == TestRunner.UNITTESTS) {
                csvHandler.gatherCsvInformation(inspectionName, holder, psiFile, unittestCsvMap)
            } else if (mode == TestRunner.PYTEST) {
                csvHandler.gatherCsvInformation(inspectionName, holder, psiFile, pytestCsvMap)
            }
        }
    }
}
