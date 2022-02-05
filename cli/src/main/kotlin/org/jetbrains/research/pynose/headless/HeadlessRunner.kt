package org.jetbrains.research.pynose.headless

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.descendants
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.lang3.tuple.MutablePair
import org.jetbrains.research.pluginUtilities.sdk.PythonMockSdk
import org.jetbrains.research.pluginUtilities.sdk.SdkConfigurer
import org.jetbrains.research.pynose.plugin.inspections.TestRunner
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.bufferedWriter
import kotlin.system.exitProcess


class HeadlessRunner : ApplicationStarter {

    override fun getCommandName() = "cli"

    private lateinit var sdk: Sdk
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

    private fun setupSdk(project: Project) {
        try {
            val projectManager = ProjectRootManager.getInstance(project)
            sdk = PythonMockSdk(project.basePath!!).create("3.10")
            val sdkConfigurer = SdkConfigurer(project, projectManager)
            sdkConfigurer.setProjectSdk(sdk)
            WriteAction.run<Throwable> {
                projectManager.projectSdk = sdk
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun gatherJsonFunctionInformation(
        inspectionName: String,
        holder: ProblemsHolder,
        jsonFileResultArray: JsonArray
    ) {
        val jsonResult = JsonObject()
        jsonResult.addProperty("Test smell name", inspectionName)
        jsonResult.addProperty("Has smell", holder.resultsArray.isNotEmpty())
        val casesMap = mutableMapOf<String, MutablePair<Int, MutableList<String>>>()
        holder.resultsArray.forEach {
            val name = PsiTreeUtil.getParentOfType(it.psiElement, PyFunction::class.java)?.name
            casesMap.getOrPut(name!!) { MutablePair(0, mutableListOf()) }
            casesMap[name]!!.left += 1
            val range = it.textRangeInElement
            if (range != null) {
                casesMap[name]!!.right.add(it.psiElement.text.substring(range.startOffset, range.endOffset))
            } else {
                casesMap[name]!!.right.add(it.psiElement.text)
            }
        }
        val entry = JsonArray()
        casesMap.forEach { (ts, pair) ->
            entry.add(ts)
            entry.add(pair.left)
            val cases = JsonArray()
            pair.right.forEach { gathered -> cases.add(gathered) }
            entry.add(cases)
        }
        jsonResult.add("Detail", entry)
        jsonFileResultArray.add(jsonResult)
    }

    private fun gatherJsonClassOrFileInformation(
        inspectionName: String,
        holder: ProblemsHolder,
        jsonFileResultArray: JsonArray
    ) {
        val jsonResult = JsonObject()
        jsonResult.addProperty("Test smell name", inspectionName)
        jsonResult.addProperty("Has smell", holder.resultsArray.isNotEmpty())
        val casesMap = mutableMapOf<String, Int>()
        holder.resultsArray.forEach { res ->
            var name = PsiTreeUtil.getParentOfType(res.psiElement, PyClass::class.java)?.name
            if (name == null) {
                name = PsiTreeUtil.getParentOfType(res.psiElement, PyFile::class.java)?.name
            }
            name?.let {
                casesMap.getOrPut(it) { 0 }
                casesMap[it] = casesMap[it]!!.plus(1)
            }
        }
        val entry = JsonArray()
        casesMap.forEach { (ts, n) ->
            entry.add(ts)
            entry.add(n)
        }
        jsonResult.add("Detail", entry)
        jsonFileResultArray.add(jsonResult)
    }

    private fun gatherCsvInformation(unittest: Boolean, name: String, holder: ProblemsHolder, psiFile: PsiFile) {
        if (unittest) {
            unittestCsvMap.getOrPut(name) { mutableSetOf() }
            if (holder.resultCount > 0) {
                unittestCsvMap[name]!!.add(psiFile)
            }
        } else {
            pytestCsvMap.getOrPut(name) { mutableSetOf() }
            if (holder.resultCount > 0) {
                pytestCsvMap[name]!!.add(psiFile)
            }
        }
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
                    analyseUniversal(inspectionManager, psiFile, jsonFileResultArray, false)
                    jsonFileResult.add("Results for file", jsonFileResultArray)
                    jsonPytestProjectResult.add(jsonFileResult)
                } else if (testRunner == TestRunner.UNITTESTS) {
                    unittestFileCount++
                    analyseUnittest(inspectionManager, psiFile, jsonFileResultArray)
                    analyseUniversal(inspectionManager, psiFile, jsonFileResultArray, true)
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
        gatherCsvInformation(false, inspectionName, holder, psiFile)
    }

    private fun analysePytest(inspectionManager: InspectionManager, psiFile: PsiFile, resultArray: JsonArray) {
        Util.getPytestInspectionsFunctionLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyFunction::class.java).forEach {
                it.accept(inspectionVisitor)
                it.descendants { true }.forEach { d -> d.accept(inspectionVisitor) }
            }
            gatherJsonFunctionInformation(inspectionName, holder, resultArray)
            gatherCsvInformation(false, inspectionName, holder, psiFile)
        }
        Util.getPytestInspectionsFileLaunchLevel().forEach { (inspection, inspectionName) ->
            analysePytestOnRequiredLevel(
                inspectionManager, psiFile, resultArray, inspection, inspectionName, ::gatherJsonFunctionInformation
            )
        }
        Util.getPytestInspectionsFileResultLevel().forEach { (inspection, inspectionName) ->
            analysePytestOnRequiredLevel(
                inspectionManager, psiFile, resultArray, inspection, inspectionName, ::gatherJsonClassOrFileInformation
            )
        }
    }

    private fun analyseUnittest(
        inspectionManager: InspectionManager,
        psiFile: PsiFile,
        jsonFileResultArray: JsonArray
    ) {
        Util.getUnittestInspectionsFunctionResultLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java).forEach {
                it.accept(inspectionVisitor)
                it.descendants { true }.forEach { d -> d.accept(inspectionVisitor) }
            }
            gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
            gatherCsvInformation(true, inspectionName, holder, psiFile)
        }
        Util.getUnittestInspectionsClassResultLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java).forEach { it.accept(inspectionVisitor) }
            gatherJsonClassOrFileInformation(inspectionName, holder, jsonFileResultArray)
            gatherCsvInformation(true, inspectionName, holder, psiFile)
        }
    }

    private fun analyseUniversal(
        inspectionManager: InspectionManager,
        psiFile: PsiFile,
        jsonFileResultArray: JsonArray,
        unittest: Boolean
    ) {
        Util.getUniversalInspections().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyCallExpression::class.java).forEach {
                it.accept(inspectionVisitor)
            }
            gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
            gatherCsvInformation(unittest, inspectionName, holder, psiFile)
        }
    }

    private fun initOutputJsonFile(outputDir: String, projectName: String): File {
        val jsonOutputFileName = "$outputDir$separator${projectName}_ext_stats.json"
        val jsonFile = File(jsonOutputFileName)
        File(outputDir).mkdirs()
        jsonFile.createNewFile()
        return jsonFile
    }

    private fun writeToJsonFile(projectResult: JsonArray, jsonFile: File) {
        val jsonString =
            GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(projectResult.toString()))
        try {
            jsonFile.writeText(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun writeToCsvFile(outputDir: String, projectName: String, ut: Boolean) {
        val sortedCsvMap = TreeMap(if (ut) unittestCsvMap else pytestCsvMap)
        val csvOutputFileName =
            "$outputDir${separator}${if (ut) "unittest" else "pytest"}${separator}${projectName}_stats.csv"
        File("$outputDir${separator}${if (ut) "unittest" else "pytest"}").mkdirs()
        File(csvOutputFileName).createNewFile()
        val writer = Paths.get(csvOutputFileName).bufferedWriter()
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
        val header = mutableListOf("project_name", "test_file_count")
        val data = mutableListOf(projectName, { if (ut) unittestFileCount else pytestFileCount }.toString())

        sortedCsvMap.keys.forEach { header.add(it) }
        sortedCsvMap.keys.forEach { data.add(sortedCsvMap[it]?.size.toString()) }
        if (!(if (ut) aggregatedUnittestHasHeader else aggregatedPytestHasHeader)) {
            agCsvUnittestData.add(header)
        }
        if (ut) aggregatedUnittestHasHeader else aggregatedPytestHasHeader = true
        (if (ut) agCsvUnittestData else agCsvPytestData).add(data)

        csvPrinter.use {
            it.printRecord(header)
            it.printRecord(data)
        }
    }

    private fun writeToAggregatedCsv(outputDir: String, testFramework: String) {
        val agCsvOutputFileName = "$outputDir${separator}$testFramework${separator}aggregated_stats.csv"
        val csvFile = File(agCsvOutputFileName)
        File("$outputDir${separator}pytest").mkdirs()
        csvFile.createNewFile()
        val writer = Paths.get(agCsvOutputFileName).bufferedWriter()
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
        if (testFramework == "pytest") {
            agCsvPytestData.forEach { csvPrinter.printRecord(it) }
        } else {
            agCsvUnittestData.forEach { csvPrinter.printRecord(it) }
        }
        csvPrinter.flush()
        csvPrinter.close()
    }

    override fun main(args: List<String>) {
        if (args.size < 2) {
            System.err.println("Specify project and output paths as arguments")
            exitProcess(0)
        }
        val repoRoot = File(args[1])
        var counter = 1
        repoRoot.listFiles()?.forEach {
            try {
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
                    setupSdk(project)
                    var i = 0
                    // прости прости прости
                    // я просто не могу понять, почему исключение вылетает и как от него избавиться
                    while (i < 10) {
                        i++
                        var success = true
                        try {
                            WriteCommandAction.runWriteCommandAction(project) {
                                val inspectionManager = InspectionManager.getInstance(project)
                                analyse(project, inspectionManager, jsonUnittestProjectResult, jsonPytestProjectResult)
                            }
                        } catch (e: Exception) {
                            success = false
                            e.printStackTrace()
                        }
                        if (success) break
                    }
                }
                val jsonUnittestFile = initOutputJsonFile("$outputDir${separator}unittest", projectName)
                val jsonPytestFile = initOutputJsonFile("$outputDir${separator}pytest", projectName)
                writeToCsvFile(outputDir, projectName, true)
                writeToCsvFile(outputDir, projectName, false)
                writeToJsonFile(jsonUnittestProjectResult, jsonUnittestFile)
                writeToJsonFile(jsonPytestProjectResult, jsonPytestFile)
                writeToAggregatedCsv(outputDir, "pytest")
                writeToAggregatedCsv(outputDir, "unittest")
                try {
                    ApplicationManager.getApplication().runWriteAction {
                        ProjectJdkTable.getInstance().removeJdk(sdk)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                println("Finished processing repo #$counter: $projectName\n")
                counter++
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        exitProcess(0)
    }
}
