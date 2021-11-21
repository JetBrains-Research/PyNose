package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyClass
import org.jetbrains.research.pynose.plugin.inspections.common.DuplicateAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class DuplicateAssertionTestSmellPytestInspection : PyInspection() {
    private val LOG = Logger.getInstance(DuplicateAssertionTestSmellPytestInspection::class.java)

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ): PsiElementVisitor {

        if (PyNoseMode.getPyNosePytestMode()) {
            return object : DuplicateAssertionTestSmellVisitor(holder, session) {
                override fun visitPyClass(node: PyClass) {
                    super.visitPyClass(node)
                    if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                        UnittestInspectionsUtils.gatherUnittestTestMethods(node)
                                .forEach { testMethod ->
                                    assertCalls.clear()
                                    assertStatements.clear()
                                    PsiTreeUtil
                                            .collectElements(testMethod) { element -> (element is PyAssertStatement) }
                                            .forEach { target -> processPyAssertStatement(target as PyAssertStatement, testMethod) }
                                    visitPyElement(testMethod)
                                }
                    }
                }
            }
        } else {
            return PsiElementVisitor.EMPTY_VISITOR
        }
    }
}