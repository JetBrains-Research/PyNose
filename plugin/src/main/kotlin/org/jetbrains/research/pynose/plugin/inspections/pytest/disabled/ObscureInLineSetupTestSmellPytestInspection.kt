package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.ObscureInLineSetupTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

class ObscureInLineSetupTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(ObscureInLineSetupTestSmellPytestInspection::class.java)
    override val inspectionName: String = "Obscure in-line setup"

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyInspectionVisitor {
        return object : ObscureInLineSetupTestSmellVisitor(holder, session) {
            override fun visitPyFile(node: PyFile) {
                super.visitPyFile(node)
                if (PytestInspectionsUtils.isValidPytestFile(node)) {
                    val testMethodLocalVarCount: MutableMap<PyFunction, MutableSet<String?>> = mutableMapOf()
                    PytestInspectionsUtils.gatherValidPytestMethods(node)
                        .forEach { testMethod -> processTestMethod(testMethod, testMethodLocalVarCount) }
                    getObscureInLineSetup(testMethodLocalVarCount)
                }
            }
        }
    }
}