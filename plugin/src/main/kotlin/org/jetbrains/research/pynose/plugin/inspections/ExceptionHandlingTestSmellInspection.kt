package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyRaiseStatement
import com.jetbrains.python.psi.PyTryExceptStatement
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ExceptionHandlingTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ExceptionHandlingTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PyInspectionVisitor {

        fun registerException(valueParam: PsiElement, offset: Int = 0, textLength: Int = valueParam.textLength) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.exception.description"),
                ProblemHighlightType.WARNING,
                TextRange(offset, textLength)
            )
        }
        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyTryExceptStatement(tryExceptStatement: PyTryExceptStatement) {
                super.visitPyTryExceptStatement(tryExceptStatement)
                if (GeneralInspectionsUtils.redirectValidParentCheck(tryExceptStatement)) {
                    registerException(tryExceptStatement, 0, "try".length)
                }
            }

            override fun visitPyRaiseStatement(raiseStatement: PyRaiseStatement) {
                super.visitPyRaiseStatement(raiseStatement)
                if (GeneralInspectionsUtils.redirectValidParentCheck(raiseStatement)) {
                    registerException(raiseStatement, 0, "raise".length)
                }
            }
        }
    }
}