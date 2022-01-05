package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.AssertionRouletteTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

class AssertionRouletteTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(AssertionRouletteTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyInspectionVisitor {
        return object : AssertionRouletteTestSmellVisitor(holder, session) {
            override fun visitPyFile(node: PyFile) {
                super.visitPyFile(node)
                if (PytestInspectionsUtils.isValidPytestFile(node)) {
                    val assertStatementsInTests: MutableMap<PyFunction, MutableSet<PyAssertStatement>> = mutableMapOf()
                    val testHasAssertionRoulette: MutableMap<PyFunction, Boolean> = mutableMapOf()
                    PytestInspectionsUtils.gatherValidPytestMethods(node)
                        .forEach { testMethod ->
                            testHasAssertionRoulette[testMethod] = false
                            PsiTreeUtil
                                .collectElements(testMethod) { element -> (element is PyAssertStatement) }
                                .forEach { target ->
                                    processPyAssertStatement(target as PyAssertStatement, testMethod, assertStatementsInTests)
                                }
                        }
                    detectAssertStatementsRoulette(assertStatementsInTests, testHasAssertionRoulette)
                    testHasAssertionRoulette.keys
                        .filter { key -> testHasAssertionRoulette[key]!! }
                        .forEach { pyFunction -> registerRoulette(pyFunction.nameIdentifier!!) }
                }
            }
        }
    }
}
