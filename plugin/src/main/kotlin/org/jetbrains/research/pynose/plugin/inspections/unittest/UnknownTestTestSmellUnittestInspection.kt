package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class UnknownTestTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(UnknownTestTestSmellUnittestInspection::class.java)
    private val assertCounts: MutableMap<PyFunction, Int> = mutableMapOf()

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : PyInspectionVisitor(holder, session) {
            fun registerUnknown(valueParam: PsiElement) {
                holder.registerProblem(
                    valueParam,
                    TestSmellBundle.message("inspections.unknown.description"),
                    ProblemHighlightType.WARNING
                )
            }

            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    UnittestInspectionsUtils.gatherUnittestTestMethods(node).forEach { testMethod ->
                        assertCounts.putIfAbsent(testMethod, 0)
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
                    assertCounts[testMethod] = assertCounts.getOrPut(testMethod) { 0 } + 1
                }
            }

            private fun processPyAssertStatement(testMethod: PyFunction) {
                assertCounts[testMethod] = assertCounts.getOrPut(testMethod) { 0 } + 1
            }
        }
    }
}