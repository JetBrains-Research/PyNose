package org.jetbrains.research.pynose.headless.inspections

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
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
import org.jetbrains.research.pynose.headless.io.csv.CsvFilesHandler
import org.jetbrains.research.pynose.headless.io.json.JsonFilesHandler
import org.jetbrains.research.pynose.plugin.inspections.TestRunner
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import java.io.File
import java.nio.file.Paths

class HeadlessInspectionAnalyzer {

    private var mode = TestRunner.UNKNOWN
    private var hasHeader = false
    private var agCsvPytestData: MutableList<MutableList<String>> = mutableListOf()
    private var agCsvUnittestData: MutableList<MutableList<String>> = mutableListOf()
    private val separator = File.separatorChar
    private val jsonHandler = JsonFilesHandler()
    private val csvHandler = CsvFilesHandler()
    private var unittestFileCount = 0
    private var pytestFileCount = 0
    private var unittestCsvMap: MutableMap<String, MutableSet<PsiFile>> = mutableMapOf()
    private var pytestCsvMap: MutableMap<String, MutableSet<PsiFile>> = mutableMapOf()

    fun analyse(
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

    fun resetParameters() {
        unittestCsvMap = mutableMapOf()
        pytestCsvMap = mutableMapOf()
        unittestFileCount = 0
        pytestFileCount = 0
    }

    fun processFileOutput(
        projectName: String, outputDir: String, jsonUnittestProjectResult: JsonArray, jsonPytestProjectResult: JsonArray
    ) {
        val jsonUnittestFile = jsonHandler.initOutputJsonFile("$outputDir${separator}unittest", projectName)
        val jsonPytestFile = jsonHandler.initOutputJsonFile("$outputDir${separator}pytest", projectName)
        csvHandler.writeToCsvFile(
            outputDir, projectName, unittestCsvMap, unittestFileCount,
            hasHeader, agCsvUnittestData, "unittest"
        )
        csvHandler.writeToCsvFile(
            outputDir, projectName, pytestCsvMap, pytestFileCount,
            hasHeader, agCsvPytestData, "pytest"
        )
        hasHeader = true
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
        inspectionManager: InspectionManager, psiFile: PsiFile, inspection: PyInspection
    ): Pair<ProblemsHolder, PsiElementVisitor> {
        val holder = ProblemsHolder(inspectionManager, psiFile, false)
        val session = LocalInspectionToolSession(psiFile, 0, psiFile.textLength)
        val inspectionVisitor = inspection.buildVisitor(holder, false, session)
        return Pair(holder, inspectionVisitor)
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
        HeadlessInspectionContainer.getPytestInspectionsFunctionLevel().forEach { 
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, it)
            PsiTreeUtil.findChildrenOfType(psiFile, PyFunction::class.java).forEach {
                it.accept(inspectionVisitor)
                it.descendants { true }.forEach { d -> d.accept(inspectionVisitor) }
            }
            jsonHandler.gatherJsonFunctionInformation(it.inspectionName, holder, resultArray)
            csvHandler.gatherCsvInformation(it.inspectionName, holder, psiFile, pytestCsvMap)
        }
        HeadlessInspectionContainer.getPytestInspectionsFileLaunchLevel().forEach {
            analysePytestOnRequiredLevel(
                inspectionManager, psiFile, resultArray,
                it, it.inspectionName, jsonHandler::gatherJsonFunctionInformation,
            )
        }
        HeadlessInspectionContainer.getPytestInspectionsFileResultLevel().forEach { 
            analysePytestOnRequiredLevel(
                inspectionManager, psiFile, resultArray,
                it, it.inspectionName, jsonHandler::gatherJsonClassOrFileInformation
            )
        }
    }

    private fun analyseUnittest(
        inspectionManager: InspectionManager, psiFile: PsiFile, jsonFileResultArray: JsonArray
    ) {
        HeadlessInspectionContainer.getUnittestInspectionsFunctionResultLevel()
            .forEach { 
                val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, it)
                PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java).forEach {
                    it.accept(inspectionVisitor)
                    it.descendants { true }.forEach { d -> d.accept(inspectionVisitor) }
                }
                jsonHandler.gatherJsonFunctionInformation(it.inspectionName, holder, jsonFileResultArray)
                csvHandler.gatherCsvInformation(it.inspectionName, holder, psiFile, unittestCsvMap)
            }
        HeadlessInspectionContainer.getUnittestInspectionsClassResultLevel().forEach { 
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, it)
            PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java).forEach { it.accept(inspectionVisitor) }
            jsonHandler.gatherJsonClassOrFileInformation(it.inspectionName, holder, jsonFileResultArray)
            csvHandler.gatherCsvInformation(it.inspectionName, holder, psiFile, unittestCsvMap)
        }
    }

    private fun analyseUniversal(
        inspectionManager: InspectionManager, psiFile: PsiFile, jsonFileResultArray: JsonArray
    ) {
        HeadlessInspectionContainer.getUniversalInspections().forEach { 
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, it)
            PsiTreeUtil.findChildrenOfType(psiFile, PyCallExpression::class.java).forEach {
                it.accept(inspectionVisitor)
            }
            jsonHandler.gatherJsonFunctionInformation(it.inspectionName, holder, jsonFileResultArray)
            if (mode == TestRunner.UNITTESTS) {
                csvHandler.gatherCsvInformation(it.inspectionName, holder, psiFile, unittestCsvMap)
            } else if (mode == TestRunner.PYTEST) {
                csvHandler.gatherCsvInformation(it.inspectionName, holder, psiFile, pytestCsvMap)
            }
        }
    }
}