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

class UnknownTestTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(UnknownTestTestSmellInspection::class.java)
    private val assertCounts: MutableMap<PyFunction, Int> = mutableMapOf()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PyElementVisitor {

        fun registerUnknown(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.unknown.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyElementVisitor() {

            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (PyNoseUtils.isValidUnittestCase(node)) {
                    PyNoseUtils.gatherTestMethods(node).forEach { testMethod ->
                        visitPyElement(testMethod)
                        if (assertCounts[testMethod] == null) {
                            assertCounts[testMethod] = 0
                        }
                    }
                    assertCounts.keys.filter { x -> assertCounts[x] == 0 }
                        .forEach { x -> registerUnknown(x.nameIdentifier!!) }
                    assertCounts.clear()
                }
            }

            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val child = callExpression.firstChild as? PyReferenceExpression ?: return
                val name = child.name
                val testMethod = PsiTreeUtil.getParentOfType(callExpression, PyFunction::class.java)
                if (name != null && name.toLowerCase().contains("assert")
                    && PyNoseUtils.isValidUnittestMethod(testMethod)
                ) {
                    if (assertCounts[testMethod!!] == null) {
                        assertCounts[testMethod] = 0
                    }
                    assertCounts[testMethod] = assertCounts[testMethod]!! + 1
                }
            }

            override fun visitPyAssertStatement(assertStatement: PyAssertStatement) {
                super.visitPyAssertStatement(assertStatement)
                val testMethod = PsiTreeUtil.getParentOfType(assertStatement, PyFunction::class.java)
                if (!PyNoseUtils.isValidUnittestMethod(testMethod)) {
                    return
                }
                if (assertCounts[testMethod!!] == null) {
                    assertCounts[testMethod] = 0
                }
                assertCounts[testMethod] = assertCounts[testMethod]!! + 1
            }
        }
    }
}