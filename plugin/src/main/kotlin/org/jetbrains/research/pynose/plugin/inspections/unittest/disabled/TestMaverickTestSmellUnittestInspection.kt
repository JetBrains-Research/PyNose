package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.inspections.PyInspectionVisitor
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.TestMaverickTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class TestMaverickTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(TestMaverickTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyInspectionVisitor {

        return object : TestMaverickTestSmellVisitor(holder, session) {

            override fun visitPyClass(pyClass: PyClass) {
                if (UnittestInspectionsUtils.isValidUnittestCase(pyClass)) {
                    val setUpFields: MutableSet<String> = mutableSetOf()
                    val testMethodSetUpFieldsUsage: MutableMap<PyFunction, MutableSet<String>> = mutableMapOf()
                    val testMethods = UnittestInspectionsUtils.gatherUnittestTestMethods(pyClass)
                    testMethods.forEach { testMethod ->
                        testMethodSetUpFieldsUsage[testMethod] = mutableSetOf()
                    }
                    processSetUpFunction(pyClass, testMethods, setUpFields, testMethodSetUpFieldsUsage)
                }
            }
        }
    }
}