package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class UnknownTestTestSmellPytestInspection : PyInspection() {
    private val LOG = Logger.getInstance(UnknownTestTestSmellPytestInspection::class.java)
    private val assertCounts: MutableMap<PyFunction, Int> = mutableMapOf()

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ): PsiElementVisitor {

        fun registerUnknown(valueParam: PsiElement) {
            holder.registerProblem(
                    valueParam,
                    TestSmellBundle.message("inspections.unknown.description"),
                    ProblemHighlightType.WARNING
            )
        }

        if (PyNoseMode.getPyNosePytestMode()) {
            return object : PyInspectionVisitor(holder, session) {

                override fun visitPyFile(node: PyFile) {
                    super.visitPyFile(node)
                    if (PytestInspectionsUtils.isValidPytestFile(node)) {
                        PytestInspectionsUtils.gatherValidPytestMethods(node)
                                .forEach { testMethod ->
                                    if (assertCounts[testMethod] == null) {
                                        assertCounts[testMethod] = 0
                                    }
                                    PsiTreeUtil
                                            .collectElements(testMethod) { element -> (element is PyAssertStatement) }
                                            .forEach { _ -> processPyAssertStatement(testMethod) }
                                }
                        assertCounts.keys.filter { x -> assertCounts[x] == 0 }
                                .forEach { x -> registerUnknown(x.nameIdentifier!!) }
                        assertCounts.clear()
                    }
                }

                private fun processPyAssertStatement(testMethod: PyFunction) {
                    if (assertCounts[testMethod] == null) {
                        assertCounts[testMethod] = 0
                    }
                    assertCounts[testMethod] = assertCounts[testMethod]!! + 1
                }
            }
        } else {
            return PsiElementVisitor.EMPTY_VISITOR
        }
    }
}