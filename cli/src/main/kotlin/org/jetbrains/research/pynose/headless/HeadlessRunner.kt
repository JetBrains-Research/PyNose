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
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
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

    override fun main(args: List<String>) {

        if (args.size < 2) {
            // redo smh
            System.err.println("Specify project path as an argument")
            exitProcess(0)
        }

        val projectRoot = args[1]
        val outputDir = args[2]
        val splitter = File.separator.replace("\\", "\\\\")
        val pathComponents = projectRoot.split(splitter)
        val outputFileName = outputDir + File.separatorChar + "PyNoseStat" + ".json";
        val file = File(outputFileName)
        println("outputFileName = $outputFileName")
        file.createNewFile()
        val projectResult = JsonArray()
        ApplicationManager.getApplication().invokeAndWait {
            val project = ProjectUtil.openProject(projectRoot, null, true) ?: return@invokeAndWait
            setupSdk(project, projectRoot)
            val inspectionManager = InspectionManager.getInstance(project)
            WriteCommandAction.runWriteCommandAction(project) {
                FilenameIndex.getAllFilesByExt(project, "py", GlobalSearchScope.projectScope(project))
                    .filter { vFile ->
                        vFile.name.startsWith("test_") || vFile.name.endsWith("_test.py")
                    }
                    .map { vFile ->
                        FilenameIndex.getFilesByName(project, vFile.name, GlobalSearchScope.projectScope(project))
                    }.forEach { psiFileList ->
                        psiFileList.forEach { psiFile ->
                            val fileResult = JsonObject()
                            fileResult.addProperty("Filename", psiFile.name)
                            val fileResultArray = JsonArray()
                            Util.getPytestInspectionsFunctionLevel().forEach { (inspection, inspectionName) ->
                                val holder = ProblemsHolder(inspectionManager, psiFile, false)
                                val session = LocalInspectionToolSession(psiFile, 0, psiFile.textLength)
                                val inspectionVisitor = inspection.buildVisitor(holder, false, session)
                                PsiTreeUtil.findChildrenOfType(psiFile, PyFunction::class.java).forEach {
                                    it.accept(inspectionVisitor)
                                }
                                val result = JsonObject()
                                result.addProperty("Test smell name", inspectionName)
                                result.addProperty("Has smell", holder.resultsArray.isNotEmpty())
                                val entry = JsonArray()
                                val casesMap = mutableMapOf<String, MutablePair<Int, MutableList<String>>>()
                                holder.resultsArray.forEach { testSmell ->
                                    val name =
                                        PsiTreeUtil.getParentOfType(testSmell.psiElement, PyFunction::class.java)?.name
                                    casesMap.getOrPut(name!!) { MutablePair(0, mutableListOf()) }
                                    casesMap[name]!!.left += 1
                                    casesMap[name]!!.right.add(testSmell.psiElement.text)
                                }
                                casesMap.forEach { (x, y) ->
                                    entry.add(x)
                                    entry.add(y.left)
                                    val cases = JsonArray()
                                    y.right.forEach {e -> cases.add(e)}
                                    entry.add(cases)
                                }
                                result.add("Detail", entry)
                                fileResultArray.add(result)
                            }
                            fileResult.add("Results for file", fileResultArray)
                            projectResult.add(fileResult)
                        }
                    }
            }
        }
        val jsonString = GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(JsonParser.parseString(projectResult.toString()))
        try {
            file.writeText(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        ApplicationManager.getApplication().runWriteAction {
            ProjectJdkTable.getInstance().removeJdk(sdk)
        }
        exitProcess(0)
    }

}

// :cli:runCliHeadless -P projectLocation="C:\Users\Olesya\PycharmProjects\PyNoseTest" -P outputDir="C:\Users\Olesya\refPyNose\PyNose\cli"