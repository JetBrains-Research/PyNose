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
import org.jetbrains.research.pynose.core.detectors.impl.TestMaverickTestSmellDetector
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class TestMaverickTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(TestMaverickTestSmellDetector::class.java)
    private val testMethodSetUpFieldsUsage: MutableMap<PyFunction, MutableSet<String>> = mutableMapOf()
    private val setUpFields: MutableSet<String> = mutableSetOf()

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        @NotNull session: LocalInspectionToolSession
    ): PyElementVisitor {

        fun registerMaverick(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.maverick.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            private var inSetUpMode: Boolean = true
            private var methodFirstParamName: String? = null

            override fun visitPyClass(node: PyClass) {
                if (PyNoseUtils.isValidUnittestCase(node)) {
                    val testMethods = PyNoseUtils.gatherTestMethods(node)
                    for (testMethod in testMethods) {
                        testMethodSetUpFieldsUsage[testMethod] = mutableSetOf()
                    }
                    val setUpFunction = node.statementList.statements
                        .filter { obj: PyStatement? -> PyFunction::class.java.isInstance(obj) }
                        .map { obj: PyStatement? -> PyFunction::class.java.cast(obj) }
                        .firstOrNull { pyFunction: PyFunction -> pyFunction.name == "setUp" }

                    if (setUpFunction != null) {
                        inSetUpMode = true
                        processPyFunction(setUpFunction)
                    }

                    val setUpClassFunction = node.statementList.statements
                        .filter { obj: PyStatement? -> PyFunction::class.java.isInstance(obj) }
                        .map { obj: PyStatement? -> PyFunction::class.java.cast(obj) }
                        .firstOrNull { pyFunction: PyFunction -> pyFunction.name == "setUpClass" }

                    if (setUpClassFunction != null) {
                        inSetUpMode = true
                        processPyFunction(setUpClassFunction)
                    }

                    inSetUpMode = false

                    for (testMethod in testMethods) {
                        PsiTreeUtil
                            .collectElements(testMethod) { r -> (r is PyReferenceExpression) }
                            .forEach { ref -> processPyReferenceExpression(ref as PyReferenceExpression) }
                        PsiTreeUtil
                            .collectElements(testMethod) { element -> (element is PyTargetExpression) }
                            .forEach { target -> processPyTargetExpression(target as PyTargetExpression) }
                    }
                    if (testMethodSetUpFieldsUsage.values.any { obj: Set<String?> -> obj.isEmpty() }
                        && setUpFields.isNotEmpty()) {
                        registerMaverick(node.nameIdentifier!!)
                    }
                }
                super.visitPyClass(node)
                testMethodSetUpFieldsUsage.clear()
                setUpFields.clear()
            }

            private fun processPyFunction(function: PyFunction) {
                if (inSetUpMode) {
                    if (function.name == "setUp" || function.name == "setUpClass") {
                        if (function.parameterList.parameters.isNotEmpty()) {
                            methodFirstParamName = function.parameterList.parameters[0].name
                        }
                    }
                    PsiTreeUtil
                        .collectElements(function) { element -> (element is PyTargetExpression) }
                        .forEach { target -> processPyTargetExpression(target as PyTargetExpression) }
                }
            }

            private fun processPyTargetExpression(targetExpression: PyTargetExpression) {
                val method = PsiTreeUtil.getParentOfType(targetExpression, PyFunction::class.java)
                if (!inSetUpMode) {
                    if (setUpFields.contains(targetExpression.text)) {
                        testMethodSetUpFieldsUsage[method]!!.add(targetExpression.text)
                    }
                    return
                }
                if (targetExpression.text.startsWith("$methodFirstParamName.")) {
                    setUpFields.add(targetExpression.text.replace("$methodFirstParamName.", "self."))
                }
            }

            private fun processPyReferenceExpression(referenceExpression: PyReferenceExpression) {
                val testMethod = PsiTreeUtil.getParentOfType(referenceExpression, PyFunction::class.java)
                if (!PyNoseUtils.isValidUnittestMethod(testMethod) || inSetUpMode
                    || !setUpFields.contains(referenceExpression.text)
                ) {
                    return
                } else {
                    testMethodSetUpFieldsUsage[testMethod]!!.add(referenceExpression.text)
                }
            }
        }
    }
}