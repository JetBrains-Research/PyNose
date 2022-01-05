package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.inspections.PyInspectionVisitor
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.ObscureInLineSetupTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class ObscureInLineSetupTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(ObscureInLineSetupTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyInspectionVisitor {
        return object : ObscureInLineSetupTestSmellVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    val testMethodLocalVarCount: MutableMap<PyFunction, MutableSet<String?>> = mutableMapOf()
                    UnittestInspectionsUtils.gatherUnittestTestMethods(node)
                        .forEach { testMethod -> processTestMethod(testMethod, testMethodLocalVarCount) }
                    getObscureInLineSetup(testMethodLocalVarCount)
                }
            }
        }
    }
}