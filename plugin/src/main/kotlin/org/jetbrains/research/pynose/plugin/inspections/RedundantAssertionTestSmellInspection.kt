package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.annotations.NotNull
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class RedundantAssertionTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(RedundantAssertionTestSmellInspection::class.java)
    private val OPERATOR_TEXT = mutableSetOf("==", "!=", ">", ">=", "<=", "<", "is")

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        @NotNull session: LocalInspectionToolSession
    ): PyElementVisitor {

        fun registerRedundant(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.redundant.assertion.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            // todo: assertTrue(4 < 4) is not detected
            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val child = callExpression.firstChild
                if (child !is PyReferenceExpression || !UnittestInspectionsUtils.isUnittestCallAssertMethod(child)
                    || !UnittestInspectionsUtils.isValidUnittestParent(callExpression)
                ) {
                    return
                }
                val argList = callExpression.getArguments(null)
                if (UnittestInspectionsUtils.ASSERT_METHOD_ONE_PARAM.containsKey(child.name)) {
                    if (argList[0].text == UnittestInspectionsUtils.ASSERT_METHOD_ONE_PARAM[child.name]) {
                        registerRedundant(callExpression)
                    }
                } else if (UnittestInspectionsUtils.ASSERT_METHOD_TWO_PARAMS.contains(child.name)) {
                    if (argList[0].text == argList[1].text) {
                        registerRedundant(callExpression)
                    }
                }
            }

            override fun visitPyAssertStatement(assertStatement: PyAssertStatement) {
                super.visitPyAssertStatement(assertStatement)
                val expressions = assertStatement.arguments
                if (expressions.isEmpty() || !UnittestInspectionsUtils.isValidUnittestParent(assertStatement)) {
                    return
                }
                if (expressions[0] is PyLiteralExpression) {
                    registerRedundant(assertStatement)
                    return
                }
                if (expressions[0] !is PyBinaryExpression) {
                    return
                }
                val binaryExpression = expressions[0] as PyBinaryExpression
                val psiOperator = binaryExpression.psiOperator ?: return
                if (binaryExpression.children.size < 2) {
                    return
                }
                if (OPERATOR_TEXT.contains(psiOperator.text) &&
                    binaryExpression.children[0].text == binaryExpression.children[1].text
                ) {
                    registerRedundant(assertStatement)
                }
            }

        }
    }
}