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
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.apache.commons.lang3.tuple.MutablePair
import org.jetbrains.research.pluginUtilities.sdk.PythonMockSdk
import org.jetbrains.research.pluginUtilities.sdk.SdkConfigurer
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess


class HeadlessRunner : ApplicationStarter {

    override fun getCommandName() = "cli"

    private lateinit var sdk: Sdk

    private fun setupSdk(project: Project, projectRoot: String) {
        val projectManager = ProjectRootManager.getInstance(project)
        sdk = PythonMockSdk(projectRoot).create("3.8")
        val sdkConfigurer = SdkConfigurer(project, projectManager)
        sdkConfigurer.setProjectSdk(sdk)
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
            casesMap[name]!!.right.add(testSmell.psiElement.text)
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

    private fun gatherJsonFileInformation(
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

    private fun initOutputFile(projectRoot: String, outputDir: String): File {
        val splitter = File.separator.replace("\\", "\\\\")
        val pathComponents = projectRoot.split(splitter)
        val jsonOutputFileName = outputDir + File.separatorChar + "PyNoseStat" + ".json";
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

    override fun main(args: List<String>) {
        if (args.size < 2) {
            System.err.println("Specify project path as an argument")
            exitProcess(0)
        }
        val projectRoot = args[1]
        val jsonFile = initOutputFile(projectRoot, args[2])
        val jsonProjectResult = JsonArray()
        ApplicationManager.getApplication().invokeAndWait {
            val project = ProjectUtil.openProject(projectRoot, null, true) ?: return@invokeAndWait
            setupSdk(project, projectRoot)
            val inspectionManager = InspectionManager.getInstance(project)
            WriteCommandAction.runWriteCommandAction(project) {
                getFiles(project).forEach { psiFileList ->
                    psiFileList.forEach { psiFile ->
                        val jsonFileResult = JsonObject()
                        jsonFileResult.addProperty("Filename", psiFile.name)
                        val jsonFileResultArray = JsonArray()
                        Util.getPytestInspectionsFunctionLevel().forEach { (inspection, inspectionName) ->
                            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
                            PsiTreeUtil.findChildrenOfType(psiFile, PyFunction::class.java).forEach {
                                it.accept(inspectionVisitor)
                            }
                            gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
                        }
                        Util.getPytestInspectionsFileLaunchLevel().forEach { (inspection, inspectionName) ->
                            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
                            psiFile.accept(inspectionVisitor)
                            gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
                        }
                        Util.getPytestInspectionsFileResultLevel().forEach { (inspection, inspectionName) ->
                            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
                            psiFile.accept(inspectionVisitor)
                            gatherJsonFileInformation(inspectionName, holder, jsonFileResultArray)
                        }
//                        Util.getUnittestInspections().forEach { (inspection, inspectionName) ->
//                            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
//                            PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java).forEach {
//                                it.accept(inspectionVisitor)
//                            }
//                            gatherJsonInformation(inspectionName, holder, jsonFileResultArray)
//                        }
                        Util.getUniversalNonRecursiveInspections().forEach { (inspection, inspectionName) ->
                            val (holder, inspectionVisitor) = initParams(inspectionManager, psiFile, inspection)
                            PsiTreeUtil.findChildrenOfType(psiFile, PyCallExpression::class.java).forEach {
                                it.accept(inspectionVisitor)
                            }
                            gatherJsonFunctionInformation(inspectionName, holder, jsonFileResultArray)
                        }
                        jsonFileResult.add("Results for file", jsonFileResultArray)
                        jsonProjectResult.add(jsonFileResult)
                    }
                }
            }
        }
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

// :cli:runCliHeadless -P projectLocation="C:\Users\Olesya\PycharmProjects\PyNoseTest" -P outputDir="C:\Users\Olesya\refPyNose\PyNose\cli"