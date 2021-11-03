package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ExceptionHandlingTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ExceptionHandlingTestSmellInspection::class.java)

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PyElementVisitor {

        fun registerException(valueParam: PsiElement, offset: Int = 0, textLength: Int = valueParam.textLength) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.exception.description"),
                ProblemHighlightType.WARNING,
                TextRange(offset, textLength)
            )
        }

        fun checkParent(element: PsiElement): Boolean {
            return (PyNoseUtils.isValidUnittestMethod(
                PsiTreeUtil.getParentOfType(
                    element,
                    PyFunction::class.java
                )
            )
                    )
        }

        return object : PyElementVisitor() {

            override fun visitPyTryExceptStatement(tryExceptStatement: PyTryExceptStatement) {
                super.visitPyTryExceptStatement(tryExceptStatement)
                if (checkParent(tryExceptStatement)) {
                    registerException(tryExceptStatement, 0, "try".length)
                }
            }

            override fun visitPyRaiseStatement(raiseStatement: PyRaiseStatement) {
                super.visitPyRaiseStatement(raiseStatement)
                if (checkParent(raiseStatement)) {
                    registerException(raiseStatement, 0, "raise".length)
                }
            }
        }
    }
}