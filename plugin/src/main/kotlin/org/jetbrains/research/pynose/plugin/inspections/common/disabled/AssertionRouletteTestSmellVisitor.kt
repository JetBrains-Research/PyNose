package org.jetbrains.research.pynose.plugin.inspections.common.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyRecursiveElementVisitor
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

open class AssertionRouletteTestSmellVisitor(
    val holder: ProblemsHolder?,
    session: LocalInspectionToolSession
) : PyRecursiveElementVisitor() {

    protected fun registerRoulette(valueParam: PsiElement) {
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.roulette.description"),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    fun detectAssertStatementsRoulette(
        assertStatementsInTests: MutableMap<PyFunction, MutableSet<PyAssertStatement>>,
        testHasAssertionRoulette: MutableMap<PyFunction, Boolean>
    ) {
        for (testMethod in assertStatementsInTests.keys) {
            val asserts: MutableSet<PyAssertStatement>? = assertStatementsInTests[testMethod]
            if (asserts!!.size < 2) {
                continue
            }
            var multipleTimes = false
            for (assertStatement in asserts) {
                val expressions = assertStatement.arguments
                if (expressions.size < 2) {
                    if (multipleTimes) {
                        testHasAssertionRoulette.replace(testMethod, true)
                    }
                    multipleTimes = true
                }
            }
        }
    }

    fun processPyAssertStatement(
        assertStatement: PyAssertStatement,
        testMethod: PyFunction,
        assertStatementsInTests: MutableMap<PyFunction, MutableSet<PyAssertStatement>>
    ) {
        assertStatementsInTests.getOrPut(testMethod) { mutableSetOf() }.add(assertStatement)
    }
}