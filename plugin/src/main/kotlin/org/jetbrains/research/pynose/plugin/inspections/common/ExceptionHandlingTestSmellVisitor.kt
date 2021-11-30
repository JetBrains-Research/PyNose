package org.jetbrains.research.pynose.plugin.inspections.common

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyRaiseStatement
import com.jetbrains.python.psi.PyTryExceptStatement
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ExceptionHandlingTestSmellVisitor(
    holder: ProblemsHolder?,
    session: LocalInspectionToolSession,
    private val quickFix: LocalQuickFix
) : PyInspectionVisitor(holder, session) {

    protected fun registerException(valueParam: PsiElement, offset: Int = 0, textLength: Int) {
        holder?.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.exception.description"),
            ProblemHighlightType.WARNING,
            TextRange(offset, textLength),
            quickFix
        )
    }

    override fun visitPyTryExceptStatement(tryExceptStatement: PyTryExceptStatement) {
        super.visitPyTryExceptStatement(tryExceptStatement)
        if (GeneralInspectionsUtils.checkValidParent(tryExceptStatement)) {
            registerException(tryExceptStatement, 0, "try".length)
        }
    }

    override fun visitPyRaiseStatement(raiseStatement: PyRaiseStatement) {
        super.visitPyRaiseStatement(raiseStatement)
        if (GeneralInspectionsUtils.checkValidParent(raiseStatement)) {
            registerException(raiseStatement, 0, "raise".length)
        }
    }
}