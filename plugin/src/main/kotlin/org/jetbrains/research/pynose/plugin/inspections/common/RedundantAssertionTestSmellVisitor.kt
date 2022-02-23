package org.jetbrains.research.pynose.plugin.inspections.common

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.impl.PyEvaluator
import org.jetbrains.research.pynose.plugin.quickfixes.common.RedundantAssertionTestSmellQuickFix
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

open class RedundantAssertionTestSmellVisitor(holder: ProblemsHolder?, session: LocalInspectionToolSession) :
    PyInspectionVisitor(holder, getContext(session)) {

    protected val OPERATOR_TEXT = mutableSetOf("==", "!=", ">", ">=", "<=", "<", "is")

    protected fun registerRedundant(valueParam: PsiElement) {
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.redundant.assertion.description"),
            ProblemHighlightType.WARNING,
            RedundantAssertionTestSmellQuickFix()
        )
    }

    protected fun <T> processAssertionArgs(expr: T, args: MutableList<PyExpression>): Boolean where T : PsiElement {
        var literalExpression: PyLiteralExpression? = null
        var binaryExpression: PyBinaryExpression? = null
        if (args[0] is PyLiteralExpression) {
            literalExpression = args[0] as PyLiteralExpression
        }
        if (args[0] is PyBinaryExpression) {
            binaryExpression = args[0] as PyBinaryExpression
        }
        if (args[0] is PyParenthesizedExpression) {
            binaryExpression = PsiTreeUtil.findChildOfType(args[0], PyBinaryExpression::class.java)
            literalExpression = PsiTreeUtil.findChildOfType(args[0], PyLiteralExpression::class.java)
        }
        if (binaryExpression != null) {
            val psiOperator = binaryExpression.psiOperator ?: return false
            if (binaryExpression.children.size < 2) {
                return false
            }
            val firstChild = binaryExpression.children[0]
            val secondChild = binaryExpression.children[1]
            val booleanEval = PyEvaluator.evaluateAsBoolean(binaryExpression)
            if (OPERATOR_TEXT.contains(psiOperator.text)
                && (booleanEval != null || (PyLiteralExpression::class.java.isAssignableFrom(firstChild::class.java)
                        && PyLiteralExpression::class.java.isAssignableFrom(secondChild::class.java)))
            ) {
                registerRedundant(expr)
                return true
            }
        }
        if (literalExpression != null) {
            registerRedundant(expr)
            return true
        }
        return false
    }

    override fun visitPyAssertStatement(assertStatement: PyAssertStatement) {
        super.visitPyAssertStatement(assertStatement)
        val args = assertStatement.arguments
        if (args.isEmpty() || !GeneralInspectionsUtils.checkValidParent(assertStatement)) {
            return
        }
        processAssertionArgs(assertStatement, args.toMutableList())
    }
}