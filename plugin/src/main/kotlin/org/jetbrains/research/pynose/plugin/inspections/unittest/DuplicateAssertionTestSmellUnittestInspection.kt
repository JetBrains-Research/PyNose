package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyReferenceExpression
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.DuplicateAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

@Suppress("UNCHECKED_CAST")
class DuplicateAssertionTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(DuplicateAssertionTestSmellUnittestInspection::class.java)

    override fun buildUnitTestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : DuplicateAssertionTestSmellVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    UnittestInspectionsUtils.gatherUnittestTestMethods(node)
                        .forEach { testMethod ->
                            processPyCallExpressions(
                                PsiTreeUtil.collectElements(testMethod) { it is PyCallExpression } as Array<PyCallExpression>
                            )
                            processPyAssertStatement(
                                PsiTreeUtil.collectElements(testMethod) { it is PyAssertStatement } as Array<PyAssertStatement>
                            )
                        }
                }
            }

            private fun processPyCallExpressions(callExpressions: Array<PyCallExpression>) {
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