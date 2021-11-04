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
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class GeneralFixtureTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(GeneralFixtureTestSmellInspection::class.java)
    private val assignmentStatementTexts: MutableSet<String> = mutableSetOf()
    private val testCaseFieldsUsage: MutableMap<PyFunction, MutableSet<String>> = mutableMapOf()

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
            var elementToCheck: Class<out PyElement?>? = null
            var methodFirstParamName: String? = null

            override fun visitPyClass(node: PyClass) {
                if (PyNoseUtils.isValidUnittestCase(node)) {
                    val setUpFunction = node.statementList.statements
                        .filter { obj: PyStatement? -> PyFunction::class.java.isInstance(obj) }
                        .map { obj: PyStatement? -> PyFunction::class.java.cast(obj) }
                        .firstOrNull { f: PyFunction ->
                            f.name == "setUp" &&
                                    f.parent is PyStatementList &&
                                    f.parent.parent is PyClass &&
                                    PyNoseUtils.isValidUnittestCase(f.parent.parent as PyClass)
                        }

                    if (setUpFunction != null) {
                        elementToCheck = PyAssignmentStatement::class.java
                        visitElement(setUpFunction)
                    }

                    val setUpClassFunction = node.statementList.statements
                        .filter { obj: PyStatement? -> PyFunction::class.java.isInstance(obj) }
                        .map { obj: PyStatement? -> PyFunction::class.java.cast(obj) }
                        .firstOrNull() { function: PyFunction ->
                            function.name == "setUpClass" &&
                                    function.parent is PyStatementList &&
                                    function.parent.parent is PyClass &&
                                    PyNoseUtils.isValidUnittestCase(function.parent.parent as PyClass)
                        }

                    if (setUpClassFunction != null) {
                        elementToCheck = PyAssignmentStatement::class.java
                        super.visitElement(setUpClassFunction)
                    }

                    elementToCheck = PyReferenceExpression::class.java
                    for (testMethod in PyNoseUtils.gatherTestMethods(node)) {
                        testCaseFieldsUsage[testMethod] = HashSet(assignmentStatementTexts)
                        super.visitElement(testMethod)
                    }
                    if (testCaseFieldsUsage.values.stream()
                            .anyMatch { strings: Set<String?> -> strings.isNotEmpty() }
                    ) {
                        registerGeneralFixture(node.nameIdentifier!!)
                    }
                }
                super.visitPyClass(node)
            }

            override fun visitPyFunction(function: PyFunction) {
                super.visitPyFunction(function)
                if (elementToCheck == PyAssignmentStatement::class.java) {
                    if (function.name == "setUp" || function.name == "setUpClass") {
                        if (function.parameterList.parameters.isNotEmpty()) {
                            methodFirstParamName = function.parameterList.parameters[0].name
                        }
                    }
                }
                for (element in function.children) {
                    visitElement(element!!)
                }
            }

            override fun visitPyAssignmentStatement(assignmentStatement: PyAssignmentStatement) {
                super.visitPyAssignmentStatement(assignmentStatement)
                if (!PyNoseUtils.isValidUnittestMethod(
                        PsiTreeUtil.getParentOfType(
                            assignmentStatement,
                            PyFunction::class.java
                        )
                    )
                ) {
                    return
                }
                if (elementToCheck != PyAssignmentStatement::class.java) {
                    for (psiElement in assignmentStatement.children) {
                        visitElement(psiElement!!)
                    }
                    return
                }
                for (expression in assignmentStatement.targets) {
                    if (expression !is PyTargetExpression) {
                        continue
                    }
                    if (expression.text.startsWith("$methodFirstParamName.")) {
                        assignmentStatementTexts.add(expression.text.replace("$methodFirstParamName.", "self."))
                    }
                }
            }

            override fun visitPyReferenceExpression(referenceExpression: PyReferenceExpression) {
                super.visitPyReferenceExpression(referenceExpression)
                val testMethod = PsiTreeUtil.getParentOfType(referenceExpression, PyFunction::class.java)
                if (!PyNoseUtils.isValidUnittestMethod(testMethod)) {
                    return
                }
                if (elementToCheck != PyReferenceExpression::class.java ||
                    !assignmentStatementTexts.contains(referenceExpression.text)
                ) {
                    for (psiElement in referenceExpression.children) {
                        visitElement(psiElement!!)
                    }
                    return
                }
                testCaseFieldsUsage[testMethod]!!.remove(referenceExpression.text)
            }
        }
    }

}