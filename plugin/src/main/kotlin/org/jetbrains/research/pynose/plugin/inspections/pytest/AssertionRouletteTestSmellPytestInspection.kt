package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.common.AssertionRouletteTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

class AssertionRouletteTestSmellPytestInspection : PyInspection() {
    private val LOG = Logger.getInstance(AssertionRouletteTestSmellPytestInspection::class.java)
    private val assertionCallsInTests: MutableMap<PyFunction, MutableSet<PyCallExpression>> = mutableMapOf()

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        return object : AssertionRouletteTestSmellVisitor(holder, session) {

            override fun visitPyFile(node: PyFile) {
                super.visitPyFile(node)
                if (PytestInspectionsUtils.isValidPytestFile(node)) {
                    PytestInspectionsUtils.gatherValidPytestMethods(node)
                        .forEach { testMethod ->
                            testHasAssertionRoulette[testMethod] = false
                            PsiTreeUtil
                                .collectElements(testMethod) { element -> (element is PyAssertStatement) }
                                .forEach { target -> processPyAssertStatement(target as PyAssertStatement, testMethod) }
                        }
                    detectAssertStatementsRoulette()
                    testHasAssertionRoulette.keys
                        .filter { key -> testHasAssertionRoulette[key]!! }
                        .forEach { pyFunction -> registerRoulette(pyFunction.nameIdentifier!!) }
                    testHasAssertionRoulette.clear()
                }
            }
        }
    }
}