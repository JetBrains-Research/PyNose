package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.annotations.NotNull
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils


class GeneralFixtureTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(GeneralFixtureTestSmellInspection::class.java)
    val assignmentStatementTexts: MutableSet<String> = mutableSetOf()
    val testCaseFieldsUsage: MutableMap<PyFunction, MutableSet<String>> = mutableMapOf()

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        @NotNull session: LocalInspectionToolSession
    ): PyElementVisitor {

        fun registerGeneralFixture(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.general.fixture.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            private var elementToCheck: Class<out PyElement?>? = null
            private var methodFirstParamName: String? = null

            override fun visitPyClass(node: PyClass) {
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    val setUpFunction = node.statementList.statements
                        .filter { obj: PyStatement? -> PyFunction::class.java.isInstance(obj) }
                        .map { obj: PyStatement? -> PyFunction::class.java.cast(obj) }
                        .firstOrNull { f: PyFunction ->
                            f.name == "setUp" &&
                                    f.parent is PyStatementList &&
                                    f.parent.parent is PyClass &&
                                    UnittestInspectionsUtils.isValidUnittestCase(f.parent.parent as PyClass)
                        }

                    if (setUpFunction != null) {
                        elementToCheck = PyAssignmentStatement::class.java
                        processPyFunction(setUpFunction)
                    }

                    val setUpClassFunction = node.statementList.statements
                        .filter { obj: PyStatement? -> PyFunction::class.java.isInstance(obj) }
                        .map { obj: PyStatement? -> PyFunction::class.java.cast(obj) }
                        .firstOrNull { function: PyFunction ->
                            function.name == "setUpClass" &&
                                    function.parent is PyStatementList &&
                                    function.parent.parent is PyClass &&
                                    UnittestInspectionsUtils.isValidUnittestCase(function.parent.parent as PyClass)
                        }

                    if (setUpClassFunction != null) {
                        elementToCheck = PyAssignmentStatement::class.java
                        processPyFunction(setUpClassFunction)
                    }

                    elementToCheck = PyReferenceExpression::class.java
                    for (testMethod in UnittestInspectionsUtils.gatherUnittestTestMethods(node)) {
                        testCaseFieldsUsage[testMethod] = HashSet(assignmentStatementTexts)
                        PsiTreeUtil
                            .collectElements(testMethod) { element -> (element is PyReferenceExpression) }
                            .forEach { ref -> processPyReferenceExpression(ref as PyReferenceExpression) }
                    }

                    if (testCaseFieldsUsage.values.any { strings: Set<String?> -> strings.isNotEmpty() }) {
                        registerGeneralFixture(node.nameIdentifier!!)
                    }
                }
                super.visitPyClass(node)
                assignmentStatementTexts.clear()
                testCaseFieldsUsage.clear()
            }

            private fun processPyFunction(function: PyFunction) {
                if (elementToCheck == PyAssignmentStatement::class.java) {
                    if (function.name == "setUp" || function.name == "setUpClass") {
                        if (function.parameterList.parameters.isNotEmpty()) {
                            methodFirstParamName = function.parameterList.parameters[0].name
                        }
                        PsiTreeUtil
                            .collectElements(function) { element -> (element is PyAssignmentStatement) }
                            .forEach { a -> processPyAssignmentStatement(a as PyAssignmentStatement) }
                    }
                }
            }

            private fun processPyAssignmentStatement(assignmentStatement: PyAssignmentStatement) {
                if (elementToCheck != PyAssignmentStatement::class.java) {
                    return
                }
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

            private fun processPyReferenceExpression(referenceExpression: PyReferenceExpression) {
                val testMethod = PsiTreeUtil.getParentOfType(referenceExpression, PyFunction::class.java)
                if (!UnittestInspectionsUtils.isValidUnittestMethod(testMethod)) {
                    return
                }
                if (elementToCheck != PyReferenceExpression::class.java ||
                    !assignmentStatementTexts.contains(referenceExpression.text)
                ) {
                    return
                }
                testCaseFieldsUsage[testMethod]!!.remove(referenceExpression.text)
            }
        }
    }
}