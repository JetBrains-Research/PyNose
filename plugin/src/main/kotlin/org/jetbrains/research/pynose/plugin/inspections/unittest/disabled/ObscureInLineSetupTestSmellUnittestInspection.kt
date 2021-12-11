package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.PyClass
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.ObscureInLineSetupTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class ObscureInLineSetupTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(ObscureInLineSetupTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : ObscureInLineSetupTestSmellVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    UnittestInspectionsUtils.gatherUnittestTestMethods(node)
                        .forEach { testMethod -> processTestMethod(testMethod) }
                    getObscureInLineSetup()
                }
            }
        }
    }
}