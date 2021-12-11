package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class UnknownTestTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(UnknownTestTestSmellPytestInspection::class.java)
    private val assertCounts: MutableMap<PyFunction, Int> = mutableMapOf()

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        fun registerUnknown(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.unknown.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyFile(node: PyFile) {
                super.visitPyFile(node)
                if (PytestInspectionsUtils.isValidPytestFile(node)) {
                    PytestInspectionsUtils.gatherValidPytestMethods(node)
                        .forEach { testMethod ->
                            assertCounts.putIfAbsent(testMethod, 0)
                            PsiTreeUtil
                                .collectElements(testMethod) { element -> (element is PyAssertStatement) }
                                .forEach { _ -> processPyAssertStatement(testMethod) }
                        }
                    assertCounts.keys
                        .filter { x -> assertCounts[x] == 0 }
                        .forEach { x -> registerUnknown(x.nameIdentifier!!) }
                    assertCounts.clear()
                }
            }

            private fun processPyAssertStatement(testMethod: PyFunction) {
                assertCounts[testMethod] = assertCounts.getOrPut(testMethod) { 0 } + 1
            }
        }
    }
}