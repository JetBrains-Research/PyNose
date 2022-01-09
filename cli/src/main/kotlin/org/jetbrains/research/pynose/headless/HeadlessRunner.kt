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
    private var fileCount = 0
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
//                ProjectJdkTableImpl.getInstance().addJdk(sdk)
                projectManager.projectSdk = sdk
            }
//            PythonSdkType.getInstance().setupSdkPaths(sdk)
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
        holder.resultsArray.forEach { testSmell ->
            val name = PsiTreeUtil.getParentOfType(testSmell.psiElement, PyFunction::class.java)?.name
            casesMap.getOrPut(name!!) { MutablePair(0, mutableListOf()) }
            casesMap[name]!!.left += 1
            val range = testSmell.textRangeInElement
            if (range != null) {
                casesMap[name]!!.right.add(testSmell.psiElement.text.substring(range.startOffset, range.endOffset))
            } else {
                casesMap[name]!!.right.add(testSmell.psiElement.text)
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
        holder.resultsArray.forEach nameCheck@{ testSmell ->
            var name = PsiTreeUtil.getParentOfType(testSmell.psiElement, PyClass::class.java)?.name
            if (name == null) {
                name = PsiTreeUtil.getParentOfType(testSmell.psiElement, PyFile::class.java)?.name
            }
            if (name == null) return@nameCheck
            casesMap.getOrPut(name) { 0 }
            casesMap[name] = casesMap[name]!!.plus(1)
        }
        val entry = JsonArray()
        casesMap.forEach { (ts, n) ->
            entry.add(ts)
            entry.add(n)
        }
        jsonResult.add("Detail", entry)
        jsonFileResultArray.add(jsonResult)
    }

    private fun gatherCsvInformation(
        unittest: Boolean,
        inspectionName: String,
        holder: ProblemsHolder,
        psiFile: PsiFile
    ) {
        if (unittest) {
            unittestCsvMap.getOrPut(inspectionName) { mutableSetOf() }
            if (holder.resultCount > 0) {
                unittestCsvMap[inspectionName]!!.add(psiFile)
            }
        } else {
            pytestCsvMap.getOrPut(inspectionName) { mutableSetOf() }
            if (holder.resultCount > 0) {
                pytestCsvMap[inspectionName]!!.add(psiFile)
            }
        }
    }

    private fun getFiles(project: Project): List<Array<PsiFile>> {
        return FilenameIndex.getAllFilesByExt(project, "py", GlobalSearchScope.projectScope(project))
            .filter { vFile ->
                vFile.name.startsWith("test_") || vFile.name.endsWith("_test.py")
            }
            .map { vFile ->
                FilenameIndex.getFilesByName(project, vFile.name, GlobalSearchScope.projectScope(project))
            }
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
        fileCount = files.count()
        files.forEach { psiFileList ->
            psiFileList.forEach { psiFile ->
                val jsonFileResult = JsonObject()
                jsonFileResult.addProperty("Filename", psiFile.name)
                val jsonFileResultArray = JsonArray()
                val testRunner = project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(psiFile)
                mode = testRunner
                if (testRunner == TestRunner.PYTEST) {
                    analysePytest(inspectionManager, psiFile, jsonFileResultArray)
                    analyseUniversal(inspectionManager, psiFile, jsonFileResultArray, false)
                    jsonFileResult.add("Results for file", jsonFileResultArray)
                    jsonPytestProjectResult.add(jsonFileResult)
                } else if (testRunner == TestRunner.UNITTESTS) {
                    analyseUnittest(inspectionManager, psiFile, jsonFileResultArray)
                    analyseUniversal(inspectionManager, psiFile, jsonFileResultArray, true)
                    jsonFileResult.add("Results for file", jsonFileResultArray)
                    jsonUnittestProjectResult.add(jsonFileResult)
                }
            }
        }
    }

    private fun analysePytest(inspectionManager: InspectionManager, psiFile: PsiFile, jsonFileResultArray: JsonArray) {
        Util.getPytestInspectionsFunctionLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyFunction::class.java).forEach {
                it.accept(inspectionVisitor)
                it.descendants { true }.forEach { d ->
                    d.accept(inspectionVisitor)
                }
            }
            gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
            gatherCsvInformation(false, inspectionName, holder, psiFile)
        }
        Util.getPytestInspectionsFileLaunchLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            psiFile.accept(inspectionVisitor)
            gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
            gatherCsvInformation(false, inspectionName, holder, psiFile)
        }
        Util.getPytestInspectionsFileResultLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            psiFile.accept(inspectionVisitor)
            gatherJsonClassOrFileInformation(inspectionName, holder, jsonFileResultArray)
            gatherCsvInformation(false, inspectionName, holder, psiFile)
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
                it.descendants { true }.forEach { d ->
                    d.accept(inspectionVisitor)
                }
            }
            gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
            gatherCsvInformation(true, inspectionName, holder, psiFile)
        }
        Util.getUnittestInspectionsClassResultLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java).forEach {
                it.accept(inspectionVisitor)
            }
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
        val jsonOutputFileName = outputDir + separator + "${projectName}_ext_stats.json"
        val jsonFile = File(jsonOutputFileName)
        jsonFile.createNewFile()
        return jsonFile
    }

    private fun writeToJsonFile(projectResult: JsonArray, jsonFile: File) {
        val jsonString = GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(JsonParser.parseString(projectResult.toString()))
        try {
            jsonFile.writeText(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun writeToCsvFile(outputDir: String, projectName: String) {
        val sortedUnittestCsvMap = TreeMap(unittestCsvMap)
        val csvUnittestOutputFileName = "$outputDir${separator}unittest${separator}${projectName}_stats.csv"
        val sortedPytestCsvMap = TreeMap(pytestCsvMap)
        val csvPytestOutputFileName = "$outputDir${separator}pytest${separator}${projectName}_stats.csv"
        File("$outputDir${separator}unittest").mkdirs()
        File(csvUnittestOutputFileName).createNewFile()
        File("$outputDir${separator}pytest").mkdirs()
        File(csvPytestOutputFileName).createNewFile()
        val unittestWriter = Paths.get(csvUnittestOutputFileName).bufferedWriter()
        val csvUnittestPrinter = CSVPrinter(unittestWriter, CSVFormat.DEFAULT)
        val unittestHeader = mutableListOf("project_name", "test_file_count")
        val unittestData = mutableListOf(projectName, fileCount.toString())
        val pytestWriter = Paths.get(csvPytestOutputFileName).bufferedWriter()
        val csvPytestPrinter = CSVPrinter(pytestWriter, CSVFormat.DEFAULT)
        val pytestHeader = mutableListOf("project_name", "test_file_count")
        val pytestData = mutableListOf(projectName, fileCount.toString())
        sortedPytestCsvMap.keys.forEach { key -> pytestHeader.add(key) }
        sortedPytestCsvMap.keys.forEach { key -> pytestData.add(sortedPytestCsvMap[key]?.size.toString()) }
        if (!aggregatedPytestHasHeader) {
            agCsvPytestData.add(pytestHeader)
        }
        aggregatedPytestHasHeader = true
        agCsvPytestData.add(pytestData)
        sortedUnittestCsvMap.keys.forEach { key -> unittestHeader.add(key) }
        sortedUnittestCsvMap.keys.forEach { key -> unittestData.add(sortedUnittestCsvMap[key]?.size.toString()) }
        if (!aggregatedUnittestHasHeader) {
            agCsvUnittestData.add(unittestHeader)
        }
        aggregatedUnittestHasHeader = true
        agCsvUnittestData.add(unittestData)
        csvUnittestPrinter.printRecord(unittestHeader)
        csvUnittestPrinter.printRecord(unittestData)
        csvUnittestPrinter.flush()
        csvUnittestPrinter.close()
        csvPytestPrinter.printRecord(pytestHeader)
        csvPytestPrinter.printRecord(pytestData)
        csvPytestPrinter.flush()
        csvPytestPrinter.close()
    }

    private fun writeToAggregatedCsv(outputDir: String, testFramework: String) {
        val agCsvOutputFileName = "$outputDir${separator}$testFramework${separator}aggregated_stats.csv"
        val csvFile = File(agCsvOutputFileName)
        File("$outputDir${separator}pytest").mkdirs()
        csvFile.createNewFile()
        val writer = Paths.get(agCsvOutputFileName).bufferedWriter()
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
        if (testFramework == "pytest") {
            agCsvPytestData.forEach { line -> csvPrinter.printRecord(line) }
        } else {
            agCsvUnittestData.forEach { line -> csvPrinter.printRecord(line) }
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
        repoRoot.listFiles()?.forEach { projectDir ->
            try {
                unittestCsvMap = mutableMapOf()
                pytestCsvMap = mutableMapOf()
                fileCount = 0
                val projectRoot = projectDir.path
                val jsonUnittestProjectResult = JsonArray()
                val jsonPytestProjectResult = JsonArray()
                var projectName = ""
                val outputDir = args[2]
                ApplicationManager.getApplication().invokeAndWait {
                    val project = ProjectUtil.openOrImport(projectRoot, null, true) ?: return@invokeAndWait
                    projectName = project.name
                    setupSdk(project)
                    var i = 0
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
                writeToCsvFile(outputDir, projectName)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        exitProcess(0)
    }
}
