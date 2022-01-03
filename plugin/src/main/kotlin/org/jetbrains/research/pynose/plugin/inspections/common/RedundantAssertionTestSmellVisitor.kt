package org.jetbrains.research.pynose.plugin.inspections.common

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.PyLiteralExpression
import com.jetbrains.python.psi.PyRecursiveElementVisitor
import org.jetbrains.research.pynose.plugin.quickfixes.common.RedundantAssertionTestSmellQuickFix
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

open class RedundantAssertionTestSmellVisitor(val holder: ProblemsHolder?, session: LocalInspectionToolSession) :
    PyRecursiveElementVisitor() {

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
        val args = assertStatement.arguments
        if (args.isEmpty() || !GeneralInspectionsUtils.checkValidParent(assertStatement)) {
            return
        }
        if (args[0] is PyLiteralExpression) {
            registerRedundant(assertStatement)
            return
        }
        if (args[0] !is PyBinaryExpression) {
            return
        }
        val binaryExpression = args[0] as PyBinaryExpression
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