package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.TestMaverickTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

class TestMaverickTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(TestMaverickTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {

        return object : TestMaverickTestSmellVisitor(holder, session) {

            override fun visitPyFile(file: PyFile) {
                super.visitPyFile(file)
                PytestInspectionsUtils.gatherPytestClasses(file).forEach { pyClass ->
                    val setUpFields: MutableSet<String> = mutableSetOf()
                    val testMethodSetUpFieldsUsage: MutableMap<PyFunction, MutableSet<String>> = mutableMapOf()
                    val testMethods = pyClass.statementList.statements
                        .filterIsInstance<PyFunction>()
                        .filter { pyFunction -> PytestInspectionsUtils.isValidPytestMethodInsideFile(pyFunction) }
                    testMethods.forEach { testMethod ->
                        testMethodSetUpFieldsUsage[testMethod] = mutableSetOf()
                    }
                    processSetUpFunction(pyClass, testMethods, setUpFields, testMethodSetUpFieldsUsage)
                }
            }
        }
    }
}