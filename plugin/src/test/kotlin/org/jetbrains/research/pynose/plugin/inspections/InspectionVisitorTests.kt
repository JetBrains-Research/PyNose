package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementVisitor
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseWithPythonSdkTest
import org.junit.jupiter.api.Test


class InspectionVisitorTests : ParametrizedBaseWithPythonSdkTest(getResourcesRootPath(::InspectionVisitorTests)) {

    @Test
    fun `test inspection's visitor independently`() {
        myFixture.configureByFile("test_default.py")
        val project: Project = myFixture.project
        val psiFile: PsiFile = myFixture.file
        val defaultTestInspection = DefaultTestTestSmellInspection()

        val inspectionManager = InspectionManager.getInstance(project)
        val holder = ProblemsHolder(inspectionManager, psiFile, false)
        val inspectionVisitor = defaultTestInspection.buildVisitor(holder, false) as PyElementVisitor

        WriteCommandAction.runWriteCommandAction(project) {
            PsiTreeUtil.findChildrenOfType(psiFile, PyClass::class.java)
                .forEach { it.accept(inspectionVisitor) }
        }

        assertNotEmpty(holder.results)
    }
}