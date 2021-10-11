package org.jetbrains.research.pynose.plugin.testinspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementVisitor
import org.jetbrains.research.pynose.plugin.inspections.DefaultTestTestSmellInspection
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.swing.SwingUtilities


class TestCLICompatibility : LightPlatformCodeInsightFixture4TestCase() {

    override fun getTestDataPath(): String {
        return "src/test/resources"
    }

    @BeforeEach
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(DefaultTestTestSmellInspection())
    }

    @AfterEach
    override fun tearDown() {
        super.tearDown()
    }

    class FileVisitor : PsiRecursiveElementVisitor() {
        private val pyClasses: MutableList<PyClass> = ArrayList()
        override fun visitElement(element: PsiElement) {
            if (element is PyClass) {
                pyClasses.add(element)
            }
            super.visitElement(element)
        }

        fun getPyClasses(): List<PyClass> {
            return pyClasses
        }
    }

    @Test
    fun testCLICompatibilityWithFixture() {
        myFixture.configureByFile("testDefault.py")
        val project: Project = myFixture.project
        val defaultInspection = DefaultTestTestSmellInspection()
        val inspectionManager = InspectionManager.getInstance(project)
        val psiFile: PsiFile = myFixture.file
        val holder = ProblemsHolder(inspectionManager, psiFile, true)
        val visitor: PyElementVisitor = defaultInspection.buildVisitor(holder, true) as PyElementVisitor
        val vis = FileVisitor()
        psiFile.accept(vis)
        val classes = vis.getPyClasses()
        for (c in classes) {
            SwingUtilities.invokeLater { visitor.visitPyClass(c) }
        }
        println(holder.results)
    }

}