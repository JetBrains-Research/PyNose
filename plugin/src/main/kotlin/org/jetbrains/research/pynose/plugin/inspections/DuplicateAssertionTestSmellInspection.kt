package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class DuplicateAssertionTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(DuplicateAssertionTestSmellInspection::class.java)
    private val assertCalls: MutableSet<Pair<String, PyFunction>> = mutableSetOf()
    private val assertStatements: MutableSet<Pair<String, PyFunction>> = mutableSetOf()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PyElementVisitor {

        fun registerDuplicate(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.duplicate.description"),
                ProblemHighlightType.WARNING
            )
        }

        fun checkParent(element: PsiElement): Boolean {
            return (PyNoseUtils.isValidUnittestMethod(
                PsiTreeUtil.getParentOfType(
                    element,
                    PyFunction::class.java
                )
            )
                    )
        }

        return object : PyElementVisitor() {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (PyNoseUtils.isValidUnittestCase(node)) {
                    PyNoseUtils.gatherTestMethods(node).forEach { testMethod ->
                        assertCalls.clear()
                        assertStatements.clear()
                        visitPyElement(testMethod)
                    }
                }
            }

            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val child = callExpression.firstChild
                val testMethod = PsiTreeUtil.getParentOfType(callExpression, PyFunction::class.java)
                if (child !is PyReferenceExpression || !PyNoseUtils.isCallAssertMethod(child)
                    || !checkParent(callExpression)
                ) {
                    return
                }
                val assertionCall = callExpression.text
                if (assertCalls.contains(Pair(assertionCall, testMethod!!))) {
                    registerDuplicate(callExpression)
                } else {
                    assertCalls.add(Pair(assertionCall, testMethod))
                }
            }

            override fun visitPyAssertStatement(assertStatement: PyAssertStatement) {
                super.visitPyAssertStatement(assertStatement)
                val assertArgs = assertStatement.arguments
                val testMethod = PsiTreeUtil.getParentOfType(assertStatement, PyFunction::class.java)
                if (assertArgs.isEmpty() || !checkParent(assertStatement)) {
                    return
                }
                val assertStatementBody = assertArgs[0].text
                if (assertStatements.contains(Pair(assertStatementBody, testMethod!!))) {
                    registerDuplicate(assertStatement)
                } else {
                    assertStatements.add(Pair(assertStatementBody, testMethod))
                }
            }
        }
    }
}