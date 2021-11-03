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

class AssertionRouletteTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(AssertionRouletteTestSmellInspection::class.java)
    private val assertionCallsInTests: MutableMap<PyFunction, MutableSet<PyCallExpression>> = mutableMapOf()
    private val assertStatementsInTests: MutableMap<PyFunction, MutableSet<PyAssertStatement>> = mutableMapOf()
    private val testHasAssertionRoulette: MutableMap<PyFunction, Boolean> = mutableMapOf()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PyElementVisitor {

        fun registerRoulette(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.roulette.description"),
                ProblemHighlightType.WARNING
            )
        }

        fun checkParent(element: PsiElement): Boolean {
            return (PyNoseUtils.isValidUnittestMethod(
                PsiTreeUtil.getParentOfType(
                    element,
                    PyFunction::class.java
                )
            )
                    )
        }

        return object : PyElementVisitor() {

            // todo: issues with highlighting in runIde mode
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (PyNoseUtils.isValidUnittestCase(node)) {
                    testHasAssertionRoulette.clear()
                    PyNoseUtils.gatherTestMethods(node).forEach { testMethod ->
                        testHasAssertionRoulette[testMethod!!] = false
                        visitPyElement(testMethod)
                    }

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
                            if (PyNoseUtils.ASSERT_METHOD_TWO_PARAMS
                                    .contains((call.firstChild as PyReferenceExpression).name) &&
                                argumentList.arguments.size < 3
                            ) {
                                testHasAssertionRoulette.replace(testMethod, true)
                            } else if (PyNoseUtils.ASSERT_METHOD_ONE_PARAM
                                    .containsKey((call.firstChild as PyReferenceExpression).name) &&
                                argumentList.arguments.size < 2
                            ) {
                                testHasAssertionRoulette.replace(testMethod, true)
                            }
                        }
                    }

                    for (testMethod in assertStatementsInTests.keys) {
                        val asserts: MutableSet<PyAssertStatement>? = assertStatementsInTests[testMethod]
                        if (asserts!!.size < 2) {
                            continue
                        }
                        for (assertStatement in asserts) {
                            val expressions = assertStatement.arguments
                            if (expressions.size < 2) {
                                testHasAssertionRoulette.replace(testMethod, true)
                            }
                        }
                    }

                    for (testMethod in assertStatementsInTests.keys) {
                        if (assertStatementsInTests[testMethod]!!.size == 1 && assertionCallsInTests[testMethod] != null
                            && assertionCallsInTests[testMethod]!!.size == 1
                        ) {
                            testHasAssertionRoulette.replace(testMethod, true)
                        }
                    }
                }
                testHasAssertionRoulette.keys
                    .filter { key -> testHasAssertionRoulette[key]!! }
                    .forEach { pyFunction -> registerRoulette(pyFunction.nameIdentifier!!) }
                testHasAssertionRoulette.clear()
            }

            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val child = callExpression.firstChild
                if (child !is PyReferenceExpression || !PyNoseUtils.isCallAssertMethod(child)
                    || !checkParent(callExpression)
                ) {
                    return
                }
                val testMethod = PsiTreeUtil.getParentOfType(callExpression, PyFunction::class.java)
                if (assertionCallsInTests[testMethod!!] == null) {
                    assertionCallsInTests[testMethod] = mutableSetOf()
                }
                assertionCallsInTests[testMethod]!!.add(callExpression)
            }

            override fun visitPyAssertStatement(assertStatement: PyAssertStatement) {
                super.visitPyAssertStatement(assertStatement)
                if (!checkParent(assertStatement)) {
                    return
                }
                val testMethod = PsiTreeUtil.getParentOfType(assertStatement, PyFunction::class.java)
                if (assertStatementsInTests[testMethod!!] == null) {
                    assertStatementsInTests[testMethod] = mutableSetOf()
                }
                assertStatementsInTests[testMethod]!!.add(assertStatement)
            }
        }
    }
}