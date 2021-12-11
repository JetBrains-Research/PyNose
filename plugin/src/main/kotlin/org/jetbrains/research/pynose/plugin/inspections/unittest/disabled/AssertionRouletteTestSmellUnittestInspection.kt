package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.AssertionRouletteTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class AssertionRouletteTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(AssertionRouletteTestSmellUnittestInspection::class.java)
    private val assertionCallsInTests: MutableMap<PyFunction, MutableSet<PyCallExpression>> = mutableMapOf()

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : AssertionRouletteTestSmellVisitor(holder, session) {
            override fun visitPyClass(pyClass: PyClass) {
                if (UnittestInspectionsUtils.isValidUnittestCase(pyClass)) {
                    testHasAssertionRoulette.clear()
                    assertStatementsInTests.clear()
                    assertionCallsInTests.clear()
                    UnittestInspectionsUtils.gatherUnittestTestMethods(pyClass).forEach { testMethod ->
                        testHasAssertionRoulette[testMethod] = false
                        PsiTreeUtil
                            .collectElements(testMethod) { element -> (element is PyCallExpression) }
                            .forEach { target -> processPyCallExpression(target as PyCallExpression, testMethod) }
                        PsiTreeUtil
                            .collectElements(testMethod) { element -> (element is PyAssertStatement) }
                            .forEach { target -> processPyAssertStatement(target as PyAssertStatement, testMethod) }
                    }
                    detectAssertCallsRoulette()
                    detectAssertStatementsRoulette()
                    getRoulette()
                    testHasAssertionRoulette.keys
                        .filter { key -> testHasAssertionRoulette[key]!! }
                        .forEach { pyFunction -> registerRoulette(pyFunction.nameIdentifier!!) }
                }
            }

            private fun detectAssertCallsRoulette() {
                for (testMethod in assertionCallsInTests.keys) {
                    val calls: MutableSet<PyCallExpression>? = assertionCallsInTests[testMethod]
                    if (calls!!.size < 2) {
                        continue
                    }
                    for (call in calls) {
                        val argumentList = call.argumentList
                        if (argumentList == null) {
                            LOG.warn("assertion with no argument")
                            continue
                        }
                        if (argumentList.getKeywordArgument("msg") != null) {
                            continue
                        }
                        if (UnittestInspectionsUtils.ASSERT_METHOD_TWO_PARAMS
                                .contains((call.callee as PyReferenceExpression).name) &&
                            argumentList.arguments.size < 3
                        ) {
                            testHasAssertionRoulette.replace(testMethod, true)
                        } else if (UnittestInspectionsUtils.ASSERT_METHOD_ONE_PARAM
                                .containsKey((call.callee as PyReferenceExpression).name) &&
                            argumentList.arguments.size < 2
                        ) {
                            testHasAssertionRoulette.replace(testMethod, true)
                        }
                    }
                }

            }

            private fun getRoulette() {
                for (testMethod in assertStatementsInTests.keys) {
                    if (assertStatementsInTests[testMethod]?.size == 1 && assertionCallsInTests[testMethod]?.size == 1) {
                        val callComments = assertionCallsInTests[testMethod]!!
                            .any { call -> call.argumentList?.getKeywordArgument("msg") != null }
                        val statComments = assertStatementsInTests[testMethod]!!
                            .any { stat -> stat.arguments != null && stat.arguments.size > 1 }
                        if (!statComments && !callComments) {
                            testHasAssertionRoulette.replace(testMethod, true)
                        }
                    }
                }
            }

            private fun processPyCallExpression(callExpression: PyCallExpression, testMethod: PyFunction) {
                if (callExpression.callee is PyReferenceExpression &&
                    UnittestInspectionsUtils.isUnittestCallAssertMethod(callExpression.callee as PyReferenceExpression)
                ) {
                    assertionCallsInTests.getOrPut(testMethod) { mutableSetOf() }.add(callExpression)
                }
            }
        }
    }
}