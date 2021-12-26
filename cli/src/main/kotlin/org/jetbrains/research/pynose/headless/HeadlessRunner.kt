package org.jetbrains.research.pynose.headless

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
import org.jetbrains.research.pluginUtilities.sdk.PythonMockSdk
import org.jetbrains.research.pluginUtilities.sdk.SdkConfigurer
import org.jetbrains.research.pynose.plugin.inspections.pytest.MagicNumberTestTestSmellPytestInspection
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
                            val holder = ProblemsHolder(inspectionManager, psiFile, false)
                            val session = LocalInspectionToolSession(psiFile, 0, psiFile.textLength)
//                            val unittestInspectionVisitor =
//                                MagicNumberTestTestSmellUnittestInspection().buildVisitor(holder, false, session)
//                            PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java).forEach {
//                                it.accept(unittestInspectionVisitor)
//                            }
                            val pytestInspectionVisitor =
                                MagicNumberTestTestSmellPytestInspection().buildVisitor(holder, false, session)
                            PsiTreeUtil.findChildrenOfType(psiFile, PyFunction::class.java).forEach {
                                it.accept(pytestInspectionVisitor)
                            }
                            println("In ${psiFile.name} ${holder.results.size} magic number test smells were found")
                            holder.resultsArray.forEach { x ->
                                println(PsiTreeUtil.getParentOfType(x.psiElement, PyFunction::class.java))
                            }
                        }
                    }
            }
        }
        ApplicationManager.getApplication().runWriteAction {
            ProjectJdkTable.getInstance().removeJdk(sdk)
        }
        exitProcess(0)
    }

}