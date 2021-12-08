package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyFile
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.AssertionRouletteTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

class AssertionRouletteTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(AssertionRouletteTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : AssertionRouletteTestSmellVisitor(holder, session) {
            override fun visitPyFile(node: PyFile) {
                super.visitPyFile(node)
                if (PytestInspectionsUtils.isValidPytestFile(node)) {
                    PytestInspectionsUtils.gatherValidPytestMethods(node)
                        .forEach { testMethod ->
                            testHasAssertionRoulette[testMethod] = false
                            PsiTreeUtil
                                .collectElements(testMethod) { element -> (element is PyAssertStatement) }
                                .forEach { target ->
                                    processPyAssertStatement(target as PyAssertStatement, testMethod)
                                }
                        }
                    detectAssertStatementsRoulette()
                    testHasAssertionRoulette.keys
                        .filter { key -> testHasAssertionRoulette[key]!! }
                        .forEach { pyFunction -> registerRoulette(pyFunction.nameIdentifier!!) }
                    testHasAssertionRoulette.clear()
                    assertStatementsInTests.clear()
                }
            }
        }
    }
}
