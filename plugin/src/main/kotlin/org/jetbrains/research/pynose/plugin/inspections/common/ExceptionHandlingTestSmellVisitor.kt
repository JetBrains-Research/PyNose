package org.jetbrains.research.pynose.plugin.inspections.common

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyRaiseStatement
import com.jetbrains.python.psi.PyTryExceptStatement
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.quickfixes.common.ExceptionHandlingTestSmellStatementQuickFix
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ExceptionHandlingTestSmellVisitor(holder: ProblemsHolder?, session: LocalInspectionToolSession) :
    PyInspectionVisitor(holder, session) {

    private fun registerTryExcept(valueParam: PsiElement) {
        val isUnittestMode = valueParam.project.service<TestRunnerServiceFacade>()
            .getConfiguredTestRunner(valueParam.containingFile) == "Unittests"
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.exception.description"),
            ProblemHighlightType.WARNING,
            TextRange(0, "try".length),
            ExceptionHandlingTestSmellStatementQuickFix(valueParam.containingFile, isUnittestMode)
        )
    }

    private fun registerRaise(valueParam: PsiElement) {
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.exception.description"),
            ProblemHighlightType.WARNING,
            TextRange(0, "raise".length)
        )
    }

    override fun visitPyTryExceptStatement(tryExceptStatement: PyTryExceptStatement) {
        super.visitPyTryExceptStatement(tryExceptStatement)
        if (GeneralInspectionsUtils.checkValidParent(tryExceptStatement)) {
            registerTryExcept(tryExceptStatement)
        }
    }

    override fun visitPyRaiseStatement(raiseStatement: PyRaiseStatement) {
        super.visitPyRaiseStatement(raiseStatement)
        if (GeneralInspectionsUtils.checkValidParent(raiseStatement)) {
            registerRaise(raiseStatement)
        }
    }
}