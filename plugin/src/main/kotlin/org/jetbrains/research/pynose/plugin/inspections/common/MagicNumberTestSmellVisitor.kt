package org.jetbrains.research.pynose.plugin.inspections.common

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyBinaryExpression
import com.jetbrains.python.psi.PyNumericLiteralExpression
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

open class MagicNumberTestSmellVisitor(
    holder: ProblemsHolder?,
    session: LocalInspectionToolSession
) : PyInspectionVisitor(holder, session) {

    protected val ignoredNumbers = setOf("-1", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "100")

    protected fun registerMagicNumber(valueParam: PsiElement) {
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.magic.number.description"),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    override fun visitPyAssertStatement(assertStatement: PyAssertStatement) {
        super.visitPyAssertStatement(assertStatement)
        val assertArgs = assertStatement.arguments
        if (assertArgs.isEmpty() || !GeneralInspectionsUtils.checkValidParent(assertStatement)) {
            return
        }
        if (assertArgs.any { arg ->
                (arg is PyNumericLiteralExpression && !ignoredNumbers.contains(arg.text))
                        || (arg is PyBinaryExpression
                        && arg.children.any { child ->
                    child is PyNumericLiteralExpression && !ignoredNumbers.contains(child.text)
                })
            }) {
            registerMagicNumber(assertStatement)
        }
    }
}
