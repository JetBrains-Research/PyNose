package org.jetbrains.research.pynose.plugin.util

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyCallExpression
import org.jetbrains.research.pluginUtilities.sdk.PythonMockSdk
import org.jetbrains.research.pluginUtilities.sdk.SdkConfigurer
import org.jetbrains.research.pynose.plugin.inspections.unittest.MagicNumberTestTestSmellUnittestInspection
import org.junit.Test

class HeadlessTest : BasePlatformTestCase() {

    // set yours
    private val projectRoot = "C:\\Users\\Olesya\\PycharmProjects\\PyNoseTest"
    private lateinit var sdk: Sdk

    private fun setupSdk(project : Project) {
        val projectManager = ProjectRootManager.getInstance(project)
        sdk = PythonMockSdk(projectRoot).create("3.8")
        val sdkConfigurer = SdkConfigurer(project, projectManager)
        sdkConfigurer.setProjectSdk(sdk)
    }

    @Test
    fun `test headless`() {
        ApplicationManager.getApplication().invokeAndWait {

            // throws an exception (smth about a pop-up message)
//            val project = ProjectUtil.openOrImport(projectRoot.toPath())

            val project = ProjectUtil.openProject(projectRoot, null, true) ?: return@invokeAndWait
            setupSdk(project)
            val inspectionManager = InspectionManager.getInstance(project)
            WriteCommandAction.runWriteCommandAction(project) {
                // if I add new files to the project it won't find them
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
                            val inspectionVisitor =
                                MagicNumberTestTestSmellUnittestInspection().buildVisitor(holder, false, session)
                            
                            PsiTreeUtil.findChildrenOfType(psiFile, PyAssertStatement::class.java).forEach {
                                it.accept(inspectionVisitor)
                            }
                            PsiTreeUtil.findChildrenOfType(psiFile, PyCallExpression::class.java).forEach {
                                it.accept(inspectionVisitor)
                            }

                            println("In ${psiFile.name} ${holder.results.size} magic number test smells were found")
                        }
                    }
            }
        }
        ApplicationManager.getApplication().runWriteAction {
            ProjectJdkTable.getInstance().removeJdk(sdk)
        }
    }
}