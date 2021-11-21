package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.inspections.common.DuplicateAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class DuplicateAssertionTestSmellUnittestInspection : PyInspection() {
    private val LOG = Logger.getInstance(DuplicateAssertionTestSmellUnittestInspection::class.java)

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ): PsiElementVisitor {

        if (PyNoseMode.getPyNoseUnittestMode()) {
            return object : DuplicateAssertionTestSmellVisitor(holder, session) {
                override fun visitPyClass(node: PyClass) {
                    super.visitPyClass(node)
                    if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                        UnittestInspectionsUtils.gatherUnittestTestMethods(node)
                                .forEach { testMethod ->
                                    assertCalls.clear()
                                    assertStatements.clear()
                                    PsiTreeUtil
                                            .collectElements(testMethod) { element -> (element is PyCallExpression) }
                                            .forEach { target -> processPyCallExpression(target as PyCallExpression, testMethod) }
                                    PsiTreeUtil
                                            .collectElements(testMethod) { element -> (element is PyAssertStatement) }
                                            .forEach { target -> processPyAssertStatement(target as PyAssertStatement, testMethod) }
                                    visitPyElement(testMethod)
                                }
                    }
                }

                private fun processPyCallExpression(callExpression: PyCallExpression, testMethod: PyFunction) {
                    val child = callExpression.callee
                    if (child !is PyReferenceExpression || !UnittestInspectionsUtils.isUnittestCallAssertMethod(child)) {
                        return
                    }
                    val assertionCall = callExpression.text
                    if (assertCalls.contains(Pair(assertionCall, testMethod))) {
                        registerDuplicate(callExpression)
                    } else {
                        assertCalls.add(Pair(assertionCall, testMethod))
                    }
                }
            }
        } else {
            return PsiElementVisitor.EMPTY_VISITOR
        }
    }
}