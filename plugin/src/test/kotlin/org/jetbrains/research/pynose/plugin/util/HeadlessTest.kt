package org.jetbrains.research.pynose.plugin.util

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.research.pynose.plugin.inspections.pytest.DuplicateAssertionTestSmellPytestInspection
import org.junit.Test
import java.io.File

class HeadlessTest : AbstractTestSmellInspectionTestWithSdk() {
    override fun getTestDataPath(): String {
        return "path"
    }

    @Test
    fun `test something`() {
        val projectRoot = File("C:\\Users\\Olesya\\PycharmProjects\\PyNoseTest")
        ApplicationManager.getApplication().invokeAndWait {

            // throws an exception (smth about a pop-up message)
//            val project = ProjectUtil.openOrImport(projectRoot.toPath())

            val project = ProjectUtil.openProject(projectRoot.toString(), null, true)
                ?: return@invokeAndWait
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
                            println(psiFile.name)
                            val holder = ProblemsHolder(inspectionManager, psiFile, false)
                            val session = LocalInspectionToolSession(psiFile, 0, psiFile.textLength)
                            val inspectionVisitor =
                                DuplicateAssertionTestSmellPytestInspection().buildVisitor(holder, false, session)

                            // for other types of visitors
//                            PsiTreeUtil.findChildrenOfType(psiFile, PyFile::class.java)
//                                .forEach {
//                                    it.accept(inspectionVisitor)
//                                }

                            psiFile.accept(inspectionVisitor)
                            println(holder.results)
                        }
                    }
            }
        }
    }
}