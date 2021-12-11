package org.jetbrains.research.pynose.plugin.inspections.common.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyTargetExpression
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

open class TestMaverickTestSmellVisitor(
    holder: ProblemsHolder?,
    session: LocalInspectionToolSession
) : PyInspectionVisitor(holder, session) {

    protected val testMethodSetUpFieldsUsage: MutableMap<PyFunction, MutableSet<String>> = mutableMapOf()
    protected val setUpFields: MutableSet<String> = mutableSetOf()
    private var inSetUpMode: Boolean = true
    private var methodFirstParamName: String? = null

    private fun registerMaverick(valueParam: PsiElement) {
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.maverick.description"),
            ProblemHighlightType.WARNING
        )
    }

    protected fun processSetUpFunction(pyClass: PyClass, testMethods: List<PyFunction>) {
        val setUpFunction = pyClass.statementList.statements
            .filterIsInstance<PyFunction>()
            .firstOrNull { pyFunction: PyFunction -> pyFunction.name == "setUp" }
        if (setUpFunction != null) {
            inSetUpMode = true
            processPyFunction(setUpFunction)
        }

        val setUpClassFunction = pyClass.statementList.statements
            .filterIsInstance<PyFunction>()
            .firstOrNull { pyFunction: PyFunction -> pyFunction.name == "setUpClass" }
        if (setUpClassFunction != null) {
            inSetUpMode = true
            processPyFunction(setUpClassFunction)
        }

        inSetUpMode = false
        for (testMethod in testMethods) {
            PsiTreeUtil
                .collectElements(testMethod) { r -> (r is PyReferenceExpression) }
                .forEach { ref -> processPyReferenceExpression(ref as PyReferenceExpression, testMethod) }
            PsiTreeUtil
                .collectElements(testMethod) { element -> (element is PyTargetExpression) }
                .forEach { target -> processPyTargetExpression(target as PyTargetExpression, testMethod) }
        }
        if (testMethodSetUpFieldsUsage.values.any { obj: Set<String?> -> obj.isEmpty() }
            && setUpFields.isNotEmpty()) {
            registerMaverick(pyClass.nameIdentifier!!)
        }
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
                .forEach { target -> processPyTargetExpression(target as PyTargetExpression, function) }
        }
    }

    private fun processPyTargetExpression(targetExpression: PyTargetExpression, method: PyFunction) {
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

    private fun processPyReferenceExpression(
        referenceExpression: PyReferenceExpression,
        testMethod: PyFunction
    ) {
        if (inSetUpMode || !setUpFields.contains(referenceExpression.text)) {
            return
        } else {
            testMethodSetUpFieldsUsage[testMethod]?.add(referenceExpression.text)
        }
    }

}