package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyFile
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.DuplicateAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

@Suppress("UNCHECKED_CAST")
class DuplicateAssertionTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(DuplicateAssertionTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyInspectionVisitor {
        return object : DuplicateAssertionTestSmellVisitor(holder, session) {
            override fun visitPyFile(node: PyFile) {
                super.visitPyFile(node)
                if (PytestInspectionsUtils.isValidPytestFile(node)) {
                    PytestInspectionsUtils.gatherValidPytestMethods(node)
                        .forEach { testMethod ->
                            processPyAssertStatements(
                                PsiTreeUtil.collectElements(testMethod) { it is PyAssertStatement }
                                    .map { it as PyAssertStatement }
                            )
                        }
                }
            }
        }
    }
}