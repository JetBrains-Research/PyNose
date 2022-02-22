package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.TestMaverickTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

class TestMaverickTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(TestMaverickTestSmellPytestInspection::class.java)
    override val inspectionName: String = "Test maverick"

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): TestMaverickTestSmellVisitor {

        return object : TestMaverickTestSmellVisitor(holder, session) {

            override fun visitPyFile(file: PyFile) {
                super.visitPyFile(file)
                val setUpFields: MutableSet<String> = mutableSetOf()
                val testMethodSetUpFieldsUsage: MutableMap<PyFunction, MutableSet<String>> = mutableMapOf()
                PytestInspectionsUtils.gatherPytestClasses(file).forEach { pyClass ->
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