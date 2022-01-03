package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.DuplicateAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils


class DuplicateAssertionTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(DuplicateAssertionTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyRecursiveElementVisitor {
        return object : DuplicateAssertionTestSmellVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    UnittestInspectionsUtils.gatherUnittestTestMethods(node)
                        .forEach { testMethod ->
                            processPyCallExpressions(
                                PsiTreeUtil.collectElements(testMethod) { it is PyCallExpression }
                                    .map { it as PyCallExpression }
                            )
                            processPyAssertStatements(
                                PsiTreeUtil.collectElements(testMethod) { it is PyAssertStatement }
                                    .map { it as PyAssertStatement }
                            )
                        }
                }
            }

            private fun processPyCallExpressions(callExpressions: List<PyCallExpression>) {
                val visitedCalls = HashSet<String>()
                for (callExpression in callExpressions) {
                    val child = callExpression.callee
                    if (child !is PyReferenceExpression || !UnittestInspectionsUtils.isUnittestCallAssertMethod(child)) {
                        continue
                    }
                    val assertionCall = callExpression.text
                    if (assertionCall in visitedCalls) {
                        registerDuplicate(callExpression)
                    } else {
                        visitedCalls += assertionCall
                    }
                }
            }
        }
    }
}