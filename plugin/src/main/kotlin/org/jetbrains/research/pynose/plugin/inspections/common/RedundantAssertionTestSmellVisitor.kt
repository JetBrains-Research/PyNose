package org.jetbrains.research.pynose.plugin.inspections.common

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.PyLiteralExpression
import org.jetbrains.research.pynose.plugin.quickfixes.common.RedundantAssertionTestSmellQuickFix
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

open class RedundantAssertionTestSmellVisitor(holder: ProblemsHolder?, session: LocalInspectionToolSession) : PyInspectionVisitor(holder, session) {

    private val OPERATOR_TEXT = mutableSetOf("==", "!=", ">", ">=", "<=", "<", "is")

    protected fun registerRedundant(valueParam: PsiElement) {
        holder!!.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.redundant.assertion.description"),
                ProblemHighlightType.WARNING,
                RedundantAssertionTestSmellQuickFix()
        )
    }

    override fun visitPyAssertStatement(assertStatement: PyAssertStatement) {
        super.visitPyAssertStatement(assertStatement)
        val expressions = assertStatement.arguments
        if (expressions.isEmpty() || !GeneralInspectionsUtils.checkValidParent(assertStatement)) {
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