package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import org.jetbrains.research.pynose.plugin.inspections.common.ExceptionHandlingTestSmellVisitor
import org.jetbrains.research.pynose.plugin.quickfixes.unittest.ExceptionHandlingTestSmellUnittestQuickFix
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ExceptionHandlingTestSmellUnittestInspection : PyInspection() {
    private val LOG = Logger.getInstance(ExceptionHandlingTestSmellUnittestInspection::class.java)

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ): PsiElementVisitor {

        return if (PyNoseMode.getPyNoseUnittestMode()) {
            object : ExceptionHandlingTestSmellVisitor(holder, session) {
                override fun registerException(valueParam: PsiElement, offset: Int, textLength: Int) {
                    holder.registerProblem(
                            valueParam,
                            TestSmellBundle.message("inspections.exception.description"),
                            ProblemHighlightType.WARNING,
                            TextRange(offset, textLength),
                            ExceptionHandlingTestSmellUnittestQuickFix()
                    )
                }
            }
        } else {
            PsiElementVisitor.EMPTY_VISITOR
        }
    }
}