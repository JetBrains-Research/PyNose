package org.jetbrains.research.pynose.plugin.inspections.obscure

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils


class GeneralFixtureTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(GeneralFixtureTestSmellInspection::class.java)
    val assignmentStatementTexts: MutableSet<String> = mutableSetOf()
    val testCaseFieldsUsage: MutableMap<PyFunction, MutableSet<String>> = mutableMapOf()

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        fun registerGeneralFixture(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.general.fixture.description"),
                ProblemHighlightType.WEAK_WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    processSetUpFunction(node)
                    processSetUpClassFunction(node)

                    for (testMethod in UnittestInspectionsUtils.gatherUnittestTestMethods(node)) {
                        testCaseFieldsUsage[testMethod] = HashSet(assignmentStatementTexts)
                        PsiTreeUtil
                            .collectElements(testMethod) { element -> (element is PyReferenceExpression) }
                            .forEach { target ->
                                processPyReferenceExpression(
                                    target as PyReferenceExpression,
                                    testMethod
                                )
                            }
                    }

                    if (testCaseFieldsUsage.values.any { strings: Set<String?> -> strings.isNotEmpty() }) {
                        registerGeneralFixture(node.nameIdentifier!!)
                    }
                }
                assignmentStatementTexts.clear()
                testCaseFieldsUsage.clear()
            }

            private fun processSetUpFunction(node: PyClass) {
                val setUpFunction = node.statementList.statements
                    .filterIsInstance<PyFunction>()
                    .map { obj: PyStatement? -> PyFunction::class.java.cast(obj) }
                    .firstOrNull { function: PyFunction ->
                        function.name == "setUp" &&
                                function.parent is PyStatementList &&
                                function.parent.parent is PyClass &&
                                UnittestInspectionsUtils.isValidUnittestCase(function.parent.parent as PyClass)
                    } ?: return
                processPyFunction(setUpFunction)
            }

            private fun processSetUpClassFunction(node: PyClass) {
                val setUpClassFunction = node.statementList.statements
                    .filterIsInstance<PyFunction>()
                    .map { obj: PyStatement? -> PyFunction::class.java.cast(obj) }
                    .firstOrNull { function: PyFunction ->
                        function.name == "setUpClass" &&
                                function.parent is PyStatementList &&
                                function.parent.parent is PyClass &&
                                UnittestInspectionsUtils.isValidUnittestCase(function.parent.parent as PyClass)
                    }

                if (setUpClassFunction != null) {
                    processPyFunction(setUpClassFunction)
                }
            }

            private fun processPyFunction(function: PyFunction) {
                var methodFirstParamName: String? = null
                if (function.name == "setUp" || function.name == "setUpClass") {
                    if (function.parameterList.parameters.isNotEmpty()) {
                        methodFirstParamName = function.parameterList.parameters[0].name
                    }
                    PsiTreeUtil
                        .collectElements(function) { element -> (element is PyAssignmentStatement) }
                        .forEach { target ->
                            processPyAssignmentStatement(target as PyAssignmentStatement, methodFirstParamName)
                        }
                }
            }

            private fun processPyAssignmentStatement(
                assignmentStatement: PyAssignmentStatement,
                methodFirstParamName: String?
            ) {
                for (expression in assignmentStatement.targets) {
                    if (expression !is PyTargetExpression) {
                        continue
                    }
                    if (expression.text.startsWith("$methodFirstParamName.")) {
                        assignmentStatementTexts.add(
                            expression.text.replace(
                                "$methodFirstParamName.",
                                "self."
                            )
                        )
                    }
                }
            }

            private fun processPyReferenceExpression(
                referenceExpression: PyReferenceExpression,
                testMethod: PyFunction
            ) {
                if (!assignmentStatementTexts.contains(referenceExpression.text)) {
                    return
                }
                testCaseFieldsUsage[testMethod]?.remove(referenceExpression.text)
            }
        }
    }
}