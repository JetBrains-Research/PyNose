package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class UnknownTestTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(UnknownTestTestSmellInspection::class.java)
    private val assertCounts: MutableMap<PyFunction, Int> = mutableMapOf()

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PyElementVisitor {

        fun registerUnknown(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.unknown.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {

            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    UnittestInspectionsUtils.gatherUnittestTestMethods(node).forEach { testMethod ->
                        visitPyElement(testMethod)
                        if (assertCounts[testMethod] == null) {
                            assertCounts[testMethod] = 0
                        }
                        PsiTreeUtil
                            .collectElements(testMethod) { element -> (element is PyCallExpression) }
                            .forEach { target -> processPyCallExpression(target as PyCallExpression, testMethod) }
                        PsiTreeUtil
                            .collectElements(testMethod) { element -> (element is PyAssertStatement) }
                            .forEach { _ -> processPyAssertStatement(testMethod) }
                    }
                    assertCounts.keys.filter { x -> assertCounts[x] == 0 }
                        .forEach { x -> registerUnknown(x.nameIdentifier!!) }
                    assertCounts.clear()
                }
            }

            private fun processPyCallExpression(callExpression: PyCallExpression, testMethod: PyFunction) {
                val child = callExpression.callee as? PyReferenceExpression ?: return
                val name = child.name
                if (name != null && name.toLowerCase().contains("assert")) {
                    if (assertCounts[testMethod] == null) { // todo: similar to get or put?
                        assertCounts[testMethod] = 0
                    }
                    assertCounts[testMethod] = assertCounts[testMethod]!! + 1
                }
            }

            private fun processPyAssertStatement(testMethod: PyFunction) {
                if (assertCounts[testMethod] == null) {
                    assertCounts[testMethod] = 0
                }
                assertCounts[testMethod] = assertCounts[testMethod]!! + 1
            }
        }
    }
}