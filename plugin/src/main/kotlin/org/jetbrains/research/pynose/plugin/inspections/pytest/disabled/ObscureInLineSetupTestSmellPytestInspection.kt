package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.PyFile
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.ObscureInLineSetupTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

class ObscureInLineSetupTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(ObscureInLineSetupTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : ObscureInLineSetupTestSmellVisitor(holder, session) {
            override fun visitPyFile(node: PyFile) {
                super.visitPyFile(node)
                if (PytestInspectionsUtils.isValidPytestFile(node)) {
                    PytestInspectionsUtils.gatherValidPytestMethods(node)
                        .forEach { testMethod -> processTestMethod(testMethod) }
                    getObscureInLineSetup()
                }
            }
        }
    }
}