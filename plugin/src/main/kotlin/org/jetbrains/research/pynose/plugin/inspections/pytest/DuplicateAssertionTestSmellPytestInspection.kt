package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyFile
import org.jetbrains.research.pynose.plugin.inspections.common.DuplicateAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

class DuplicateAssertionTestSmellPytestInspection : PyInspection() {
    private val LOG = Logger.getInstance(DuplicateAssertionTestSmellPytestInspection::class.java)

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ): PsiElementVisitor {

        if (PyNoseMode.getPyNosePytestMode()) {
            return object : DuplicateAssertionTestSmellVisitor(holder, session) {
                override fun visitPyFile(node: PyFile) {
                    super.visitPyFile(node)
                    if (PytestInspectionsUtils.isValidPytestFile(node)) {
                        PytestInspectionsUtils.gatherValidPytestMethods(node)
                                .forEach { testMethod ->
                                    assertCalls.clear()
                                    assertStatements.clear()
                                    PsiTreeUtil
                                            .collectElements(testMethod) { element -> (element is PyAssertStatement) }
                                            .forEach { target -> processPyAssertStatement(target as PyAssertStatement, testMethod) }
                                }
                    }
                }
            }
        } else {
            return PsiElementVisitor.EMPTY_VISITOR
        }
    }
}