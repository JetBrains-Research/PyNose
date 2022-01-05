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
    private lateinit var mode: TestRunner
    private var fileCount = 0
    private val unittestCsvMap: MutableMap<String, Int> = mutableMapOf()
    private val pytestCsvMap: MutableMap<String, Int> = mutableMapOf()

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
        holder.resultsArray.forEach { testSmell ->
            var name = PsiTreeUtil.getParentOfType(testSmell.psiElement, PyClass::class.java)?.name
            if (name == null) {
                name = PsiTreeUtil.getParentOfType(testSmell.psiElement, PyFile::class.java)?.name
            }
            casesMap.getOrPut(name!!) { 0 }
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

    private fun gatherCsvInformation(unittest: Boolean, inspectionName: String, holder: ProblemsHolder) {
        if (unittest) {
            unittestCsvMap.getOrPut(inspectionName) { 0 }
            unittestCsvMap[inspectionName] = unittestCsvMap[inspectionName]!!.plus(holder.resultCount)
        } else {
            pytestCsvMap.getOrPut(inspectionName) { 0 }
            pytestCsvMap[inspectionName] = pytestCsvMap[inspectionName]!!.plus(holder.resultCount)
        }
    }

    private fun initOutputJsonFile(outputDir: String): File {
        val jsonOutputFileName = outputDir + File.separatorChar + "PyNoseStats.json";
        val jsonFile = File(jsonOutputFileName)
        println("jsonOutputFileName = $jsonOutputFileName")
        jsonFile.createNewFile()
        return jsonFile
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

    private fun analyse(project: Project, inspectionManager: InspectionManager, jsonProjectResult: JsonArray) {
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
                } else if (testRunner == TestRunner.UNITTESTS) {
                    analyseUnittest(inspectionManager, psiFile, jsonFileResultArray)
                    analyseUniversal(inspectionManager, psiFile, jsonFileResultArray, true)
                }
                jsonFileResult.add("Results for file", jsonFileResultArray)
                jsonProjectResult.add(jsonFileResult)
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
            gatherCsvInformation(false, inspectionName, holder)
        }
        Util.getPytestInspectionsFileLaunchLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            psiFile.accept(inspectionVisitor)
            gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
            gatherCsvInformation(false, inspectionName, holder)
        }
        Util.getPytestInspectionsFileResultLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            psiFile.accept(inspectionVisitor)
            gatherJsonClassOrFileInformation(inspectionName, holder, jsonFileResultArray)
            gatherCsvInformation(false, inspectionName, holder)
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
            gatherCsvInformation(true, inspectionName, holder)
        }
        Util.getUnittestInspectionsClassResultLevel().forEach { (inspection, inspectionName) ->
            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
            PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java).forEach {
                it.accept(inspectionVisitor)
            }
            gatherJsonClassOrFileInformation(inspectionName, holder, jsonFileResultArray)
            gatherCsvInformation(true, inspectionName, holder)
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
            gatherCsvInformation(unittest, inspectionName, holder)
        }
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
        var csvOutputFileName = ""
        var csvOutputDirName = ""
        var sortedPytestCsvMap: MutableMap<String, Int> = mutableMapOf()
        var sortedUnittestCsvMap: MutableMap<String, Int> = mutableMapOf()
        if (mode == TestRunner.PYTEST) {
            sortedPytestCsvMap = TreeMap(pytestCsvMap)
            csvOutputDirName = "$outputDir/pytest"
            csvOutputFileName = "$outputDir/pytest/${projectName}_stats.csv"
        } else if (mode == TestRunner.UNITTESTS) {
            sortedUnittestCsvMap = TreeMap(unittestCsvMap)
            csvOutputDirName = "$outputDir/unittest"
            csvOutputFileName = "$outputDir/unittest/${projectName}_stats.csv"
        }
        val csvFile = File(csvOutputFileName)
        val csvDir = File(csvOutputDirName)
        csvDir.mkdirs()
        println("csvOutputFileName = $csvOutputFileName")
        csvFile.createNewFile()
        val writer = Paths.get(csvOutputFileName).bufferedWriter()
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
        val headers = mutableListOf("project_name", "test_file_count")
        val data = mutableListOf(projectName, fileCount.toString())
        if (mode == TestRunner.PYTEST) {
            sortedPytestCsvMap.keys.forEach { key -> headers.add(key) }
            sortedPytestCsvMap.keys.forEach { key -> data.add(sortedPytestCsvMap[key].toString()) }
        } else if (mode == TestRunner.UNITTESTS) {
            sortedUnittestCsvMap.keys.forEach { key -> headers.add(key) }
            sortedUnittestCsvMap.keys.forEach { key -> data.add(sortedUnittestCsvMap[key].toString()) }
        }
        csvPrinter.printRecord(headers)
        csvPrinter.printRecord(data)
        csvPrinter.flush()
        csvPrinter.close()
    }

    override fun main(args: List<String>) {
        if (args.size < 2) {
            System.err.println("Specify project path as an argument")
            exitProcess(0)
        }
        val projectRoot = args[1]
        val jsonFile = initOutputJsonFile(args[2])
        val jsonProjectResult = JsonArray()
        var projectName = ""
        ApplicationManager.getApplication().invokeAndWait {
            val project = ProjectUtil.openProject(projectRoot, null, true) ?: return@invokeAndWait
            projectName = project.name
            setupSdk(project, projectRoot)
            val inspectionManager = InspectionManager.getInstance(project)
            WriteCommandAction.runWriteCommandAction(project) {
                analyse(project, inspectionManager, jsonProjectResult)
            }
        }
        writeToCsvFile(args[2], projectName)
        writeToJsonFile(jsonProjectResult, jsonFile)
        try {
            ApplicationManager.getApplication().runWriteAction {
                ProjectJdkTable.getInstance().removeJdk(sdk)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        exitProcess(0)
    }
}
